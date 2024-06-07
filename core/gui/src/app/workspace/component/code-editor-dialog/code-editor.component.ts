import {AfterViewInit, Component, ComponentRef, ElementRef, OnDestroy, Renderer2, ViewChild} from "@angular/core";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {WorkflowActionService} from "../../service/workflow-graph/model/workflow-action.service";
import {WorkflowVersionService} from "src/app/dashboard/user/service/workflow-version/workflow-version.service";
import {YText} from "yjs/dist/src/types/YText";
import {MonacoBinding} from "y-monaco";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
// import { MonacoLanguageClient } from "monaco-languageclient";
import {CoeditorPresenceService} from "../../service/workflow-graph/model/coeditor-presence.service";
import {DomSanitizer, SafeStyle} from "@angular/platform-browser";
import {Coeditor} from "../../../common/type/user";
import {YType} from "../../types/shared-editing.interface";
import {isUndefined} from "lodash";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api.js";
import {editor} from "monaco-editor/esm/vs/editor/editor.api.js";
import {FormControl} from "@angular/forms";
import {MonacoBreakpoint} from "monaco-breakpoints";
import {WorkflowWebsocketService} from "../../service/workflow-websocket/workflow-websocket.service";
import {ExecuteWorkflowService} from "../../service/execute-workflow/execute-workflow.service";
import {BreakpointManager, UdfDebugService} from "../../service/operator-debug/udf-debug.service";
import {isDefined} from "../../../common/util/predicate";
import {ConsoleUpdateEvent} from "../../types/workflow-common.interface";
import {EditorMouseEvent, EditorMouseTarget} from "monaco-breakpoints/dist/types";
import MouseTargetType = editor.MouseTargetType;

/**
 * CodeEditorComponent is the content of the dialogue invoked by CodeareaCustomTemplateComponent.
 *
 * It contains a shared-editable Monaco editor. When the dialogue is invoked by
 * the button in CodeareaCustomTemplateComponent, this component will use the actual y-text of the code within the
 * operator property to connect to the editor.
 *
 */
@UntilDestroy()
@Component({
  selector: "texera-code-editor",
  templateUrl: "code-editor.component.html",
  styleUrls: ["code-editor.component.scss"],
})
export class CodeEditorComponent implements AfterViewInit, SafeStyle, OnDestroy {
  @ViewChild("editor", { static: true }) editorElement!: ElementRef;
  @ViewChild("container", { static: true }) containerElement!: ElementRef;
  private code?: YText;
  private editor?: any;
  private languageServerSocket?: WebSocket;
  private workflowVersionStreamSubject: Subject<void> = new Subject<void>();
  private operatorID!: string;
  private breakpointManager: BreakpointManager | undefined;
  public title: string | undefined;
  public formControl!: FormControl;
  public componentRef: ComponentRef<CodeEditorComponent> | undefined;
  public language: string = "";
  public languageTitle: string = "";
  public isUpdatingBreakpoints = false;
  public instance: MonacoBreakpoint | undefined = undefined;
  private tooltipValue = "";
  private generateLanguageTitle(language: string): string {
    return `${language.charAt(0).toUpperCase()}${language.slice(1)} UDF`;
  }

  changeLanguage(newLanguage: string) {
    this.language = newLanguage;
    console.log("change to ", newLanguage);
    if (this.editor) {
      monaco.editor.setModelLanguage(this.editor.getModel(), newLanguage);
    }
    this.languageTitle = this.generateLanguageTitle(newLanguage);
  }


  constructor(
    private sanitizer: DomSanitizer,
    private workflowActionService: WorkflowActionService,
    private workflowVersionService: WorkflowVersionService,
    public coeditorPresenceService: CoeditorPresenceService,
    public executeWorkflowService: ExecuteWorkflowService,
    public workflowWebsocketService: WorkflowWebsocketService,
    public udfDebugService: UdfDebugService,
    private elementRef: ElementRef,
    private renderer: Renderer2
  ) {
    const currentOperatorId = this.workflowActionService.getJointGraphWrapper().getCurrentHighlightedOperatorIDs()[0];
    const operatorType = this.workflowActionService.getTexeraGraph().getOperator(currentOperatorId).operatorType;

    console.log(operatorType);
    if (operatorType === "RUDFSource" || operatorType === "RUDF") {
      this.changeLanguage("r");
    } else if (operatorType === "PythonUDFV2" || operatorType === "PythonUDFSourceV2") {
      this.changeLanguage("python");
    } else {
      this.changeLanguage("java");
    }
  }

  ngAfterViewInit() {
    this.workflowActionService.getTexeraGraph().updateSharedModelAwareness("editingCode", true);
    this.operatorID = this.workflowActionService.getJointGraphWrapper().getCurrentHighlightedOperatorIDs()[0];
    this.title = this.workflowActionService.getTexeraGraph().getOperator(this.operatorID).customDisplayName;
    const style = localStorage.getItem(this.operatorID);
    if (style) this.containerElement.nativeElement.style.cssText = style;
    this.code = (
      this.workflowActionService
        .getTexeraGraph()
        .getSharedOperatorType(this.operatorID)
        .get("operatorProperties") as YType<Readonly<{ [key: string]: any }>>
    ).get("code") as YText;

    this.breakpointManager = this.udfDebugService.getOrCreateManager(this.operatorID);

    this.workflowVersionService
      .getDisplayParticularVersionStream()
      .pipe(takeUntil(this.workflowVersionStreamSubject))
      .subscribe((displayParticularVersion: boolean) => {
        if (displayParticularVersion) {
          this.initDiffEditor();
        } else {
          this.initMonaco();
          this.formControl.statusChanges.pipe(untilDestroyed(this)).subscribe(_ => {
            this.editor.updateOptions({
              readOnly: this.formControl.disabled,
            });
          });
        }
      });


    this.workflowWebsocketService
      .subscribeToEvent("ConsoleUpdateEvent")
      .pipe(untilDestroyed(this))
      .subscribe((pythonConsoleUpdateEvent: ConsoleUpdateEvent) => {
        const operatorId = pythonConsoleUpdateEvent.operatorId;
        pythonConsoleUpdateEvent.messages
          .filter(consoleMessage => consoleMessage.msgType.name === "DEBUGGER")
          .map(
          consoleMessage => {
            console.log(consoleMessage);
            const pattern = /^Breakpoint (\d+).*\.py:(\d+)\s*$/
            return consoleMessage.title.match(pattern)
          },
        )
          .filter(isDefined)
          .forEach(match => {
          const breakpoint = Number(match[1]);
          const lineNumber = Number(match[2]);
          this.breakpointManager?.assignBreakpointId(lineNumber, breakpoint);
        });
      });

    this.editor.onContextMenu((e:EditorMouseEvent) =>{
      const { type, range, detail, position } = this.getMouseEventTarget(e);

      if (type === MouseTargetType.GUTTER_GLYPH_MARGIN) {
        // This indicates that the current position of the mouse is over the total number of lines in the editor
        if (detail.isAfterLines) {
          return;
        }
        this.showTooltip(e.event.posx, e.event.posy);
      }
    })
  }

  private getMouseEventTarget(e: EditorMouseEvent) {
    return { ...(e.target as EditorMouseTarget) };
  }

  showTooltip(mouseX: number, mouseY: number): void {
    // Create tooltip element
    const tooltip = this.renderer.createElement('div');
    this.renderer.addClass(tooltip, 'custom-tooltip');

    // Create content container for the tooltip
    const contentContainer = this.renderer.createElement('div');
    this.renderer.addClass(contentContainer, 'tooltip-content');

    // Create value display element
    const valueDisplay = this.renderer.createElement('span');
    this.renderer.addClass(valueDisplay, 'tooltip-value');
    this.renderer.setProperty(valueDisplay, 'innerText', this.tooltipValue || 'Click to edit');

    // Create edit icon
    const editIcon = this.renderer.createElement('span');
    this.renderer.addClass(editIcon, 'edit-icon');
    this.renderer.setProperty(editIcon, 'innerHTML', '&#9998;'); // Unicode character for pencil

    // Create input element
    const input = this.renderer.createElement('input');
    this.renderer.addClass(input, 'custom-input');
    this.renderer.setProperty(input, 'value', this.tooltipValue);

    // Append elements to content container
    this.renderer.appendChild(contentContainer, valueDisplay);
    this.renderer.appendChild(contentContainer, input);
    this.renderer.appendChild(contentContainer, editIcon);

    // Append content container to tooltip
    this.renderer.appendChild(tooltip, contentContainer);

    // Append tooltip to the document body
    this.renderer.appendChild(document.body, tooltip);

    // Function to toggle edit mode
    const toggleEditMode = (isEditMode: boolean) => {
      if (isEditMode) {
        this.renderer.setStyle(input, 'display', 'block');
        this.renderer.setStyle(valueDisplay, 'display', 'none');
        input.focus();
      } else {
        this.tooltipValue = input.value;
        this.renderer.setStyle(input, 'display', 'none');
        this.renderer.setStyle(valueDisplay, 'display', 'block');
        this.renderer.setProperty(valueDisplay, 'innerText', this.tooltipValue || 'Click to edit');
      }
    };

    // Set initial state based on tooltip value
    if (this.tooltipValue === '') {
      toggleEditMode(true);
    } else {
      toggleEditMode(false);
    }

    // Add click event to edit icon
    this.renderer.listen(editIcon, 'click', () => {
      toggleEditMode(true);
    });

    // Listen for Enter key press to exit edit mode
    this.renderer.listen(input, 'keydown', (event: KeyboardEvent) => {
      if (event.key === 'Enter') {
        const inputValue = (event.target as HTMLInputElement).value;
        console.log(`User input: ${inputValue}`);
        toggleEditMode(false);
      }
    });

    // Calculate tooltip dimensions after appending to the DOM
    const tooltipRect = tooltip.getBoundingClientRect();

    // Adjust the position to appear at the left side of the mouse
    const adjustedX = mouseX - tooltipRect.width - 10; // Subtracting width and adding some offset to the left
    const adjustedY = mouseY - tooltipRect.height / 2;

    // Update tooltip position
    this.renderer.setStyle(tooltip, 'top', `${adjustedY}px`);
    this.renderer.setStyle(tooltip, 'left', `${adjustedX}px`);
  }

  ngOnDestroy(): void {
    this.workflowActionService.getTexeraGraph().updateSharedModelAwareness("editingCode", false);
    localStorage.setItem(this.operatorID, this.containerElement.nativeElement.style.cssText);
    if (
      this.languageServerSocket !== undefined &&
      this.languageServerSocket.readyState === this.languageServerSocket.OPEN
    ) {
      this.languageServerSocket.close();
      this.languageServerSocket = undefined;
    }

    if (this.editor !== undefined) {
      this.editor.dispose();
    }

    if (!isUndefined(this.workflowVersionStreamSubject)) {
      this.workflowVersionStreamSubject.next();
      this.workflowVersionStreamSubject.complete();
    }
  }

  private restoreBreakpoints(instance: MonacoBreakpoint, lineNums: number[]) {
    console.log("trying to restore " + lineNums);
    this.isUpdatingBreakpoints = true;
    instance["lineNumberAndDecorationIdMap"].forEach((v:string,k:number,m:Map<number, string>) =>{
      instance["removeSpecifyDecoration"](v, k);
    })
    for (let lineNumber of lineNums) {
        const range: monaco.IRange = {
          startLineNumber: lineNumber,
          endLineNumber: lineNumber,
          startColumn: 0,
          endColumn: 0,
        };
        instance["createSpecifyDecoration"](range);
    }
    this.isUpdatingBreakpoints = false;
  }

  /**
   * Specify the co-editor's cursor style. This step is missing from MonacoBinding.
   * @param coeditor
   */
  public getCoeditorCursorStyles(coeditor: Coeditor) {
    const textCSS =
      "<style>" +
      `.yRemoteSelection-${coeditor.clientId} { background-color: ${coeditor.color?.replace("0.8", "0.5")}}` +
      `.yRemoteSelectionHead-${coeditor.clientId}::after { border-color: ${coeditor.color}}` +
      `.yRemoteSelectionHead-${coeditor.clientId} { border-color: ${coeditor.color}}` +
      "</style>";
    return this.sanitizer.bypassSecurityTrustHtml(textCSS);
  }

  /**
   * Create a Monaco editor and connect it to MonacoBinding.
   * @private
   */
  private initMonaco() {
    const editor = monaco.editor.create(this.editorElement.nativeElement, {
      language: this.language,
      fontSize: 11,
      theme: "vs-dark",
      automaticLayout: true,
      minimap: {
        enabled: false,
      },
      glyphMargin: true,
    });

    if (this.code) {
      new MonacoBinding(
        this.code,
        editor.getModel()!,
        new Set([editor]),
        this.workflowActionService.getTexeraGraph().getSharedModelAwareness(),
      );
    }
    this.editor = editor;
    if (this.language == "python") {
      // this.connectLanguageServer();
    }

    this.instance = new MonacoBreakpoint({ editor });

    (this.instance).on("breakpointChanged", lineNums => {
      if(this.isUpdatingBreakpoints){
        return;
      }
      this.breakpointManager?.setBreakpoints(lineNums);
      console.log("breakpointChanged: "+lineNums)
    });

    this.breakpointManager?.getBreakpointHitStream()
      .pipe(untilDestroyed(this))
      .subscribe(lineNum => {
        console.log("highlight " + lineNum);
        this.instance!.removeHighlight();
        if (lineNum != 0) {
          this.instance!.setLineHighlight(lineNum);
        }
      });
    this.breakpointManager?.getLineNumToBreakpointMappingStream().pipe(untilDestroyed(this)).subscribe(
      mapping =>{
        console.log("trigger"+mapping)
        this.restoreBreakpoints(this.instance!, Array.from(mapping.keys()));
    })

  }


  // private connectLanguageServer() {
  //   if (this.languageServerSocket === undefined) {
  //     this.languageServerSocket = new WebSocket(getWebsocketUrl("/python-language-server", "3000"));
  //     this.languageServerSocket.onopen = () => {
  //       if (this.languageServerSocket !== undefined) {
  //         const socket = toSocket(this.languageServerSocket);
  //         const reader = new WebSocketMessageReader(socket);
  //         const writer = new WebSocketMessageWriter(socket);
  //         // const languageClient = new MonacoLanguageClient({
  //         //   name: "Python UDF Language Client",
  //         //   clientOptions: {
  //         //     documentSelector: ["python"],
  //         //     errorHandler: {
  //         //       error: () => ({ action: ErrorAction.Continue }),
  //         //       closed: () => ({ action: CloseAction.Restart }),
  //         //     },
  //         //   },
  //         //   connectionProvider: { get: () => Promise.resolve({ reader, writer }) },
  //         // });
  //         // languageClient.start();
  //         // reader.onClose(() => languageClient.stop());
  //       }
  //     };
  //   }
  // }

  private initDiffEditor() {
    if (this.code) {
      this.editor = monaco.editor.createDiffEditor(this.editorElement.nativeElement, {
        readOnly: true,
        theme: "vs-dark",
        fontSize: 11,
        automaticLayout: true,
      });
      const currentWorkflowVersionCode = this.workflowActionService
        .getTempWorkflow()
        ?.content.operators?.filter(
          operator =>
            operator.operatorID ===
            this.workflowActionService.getJointGraphWrapper().getCurrentHighlightedOperatorIDs()[0],
        )?.[0].operatorProperties.code;
      this.editor.setModel({
        original: monaco.editor.createModel(this.code.toString(), "python"),
        modified: monaco.editor.createModel(currentWorkflowVersionCode, "python"),
      });
    }
  }

  onFocus() {
    this.workflowActionService.getJointGraphWrapper().highlightOperators(this.operatorID);
  }

  onMouseLeave(){
    if(this.instance){
      this.instance!["removeHoverDecoration"]();
    }
  }

}
