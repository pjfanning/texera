import {AfterViewInit, Component, ComponentRef, ElementRef, OnDestroy, Renderer2, ViewChild} from "@angular/core";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {WorkflowActionService} from "../../service/workflow-graph/model/workflow-action.service";
import {WorkflowVersionService} from "../../../dashboard/service/user/workflow-version/workflow-version.service";
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
import {MonacoBreakpoint} from "monaco-breakpoints";
import {WorkflowWebsocketService} from "../../service/workflow-websocket/workflow-websocket.service";
import {ExecuteWorkflowService} from "../../service/execute-workflow/execute-workflow.service";
import {BreakpointManager, UdfDebugService} from "../../service/operator-debug/udf-debug.service";
import {isDefined} from "../../../common/util/predicate";
import {ConsoleUpdateEvent} from "../../types/workflow-common.interface";
import {
  EditorMouseEvent,
  EditorMouseTarget,
} from "monaco-breakpoints/dist/types";
import MouseTargetType = editor.MouseTargetType;

export type ModelDecorationOptions = monaco.editor.IModelDecorationOptions;
export enum BreakpointEnum {
  Exist,
  Hover,
}
export const CONDITIONAL_BREAKPOINT_OPTIONS: ModelDecorationOptions = {
  glyphMarginClassName: "monaco-conditional-breakpoint",
};

export const BREAKPOINT_OPTIONS: ModelDecorationOptions = {
  glyphMarginClassName: "monaco-breakpoint",
};

export const BREAKPOINT_HOVER_OPTIONS: ModelDecorationOptions = {
  glyphMarginClassName: "monaco-hover-breakpoint",
};

export type Range = monaco.IRange;
import { FormControl } from "@angular/forms";
import { AIAssistantService, TypeAnnotationResponse } from "../../service/ai-assistant/ai-assistant.service";
import { AnnotationSuggestionComponent } from "./annotation-suggestion.component";

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
  @ViewChild(AnnotationSuggestionComponent) annotationSuggestion!: AnnotationSuggestionComponent;
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

  // Boolean to determine whether the suggestion UI should be shown
  public showAnnotationSuggestion: boolean = false;
  // The code selected by the user
  public currentCode: string = "";
  // The result returned by the backend AI assistant
  public currentSuggestion: string = "";
  // The range selected by the user
  public currentRange: monaco.Range | undefined;
  public suggestionTop: number = 0;
  public suggestionLeft: number = 0;

  public isUpdatingBreakpoints = false;
  public instance: MonacoBreakpoint | undefined = undefined;
  public lastBreakLine = 0;
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
    private aiAssistantService: AIAssistantService,
    public executeWorkflowService: ExecuteWorkflowService,
    public workflowWebsocketService: WorkflowWebsocketService,
    public udfDebugService: UdfDebugService,
    private renderer: Renderer2
  ) {
    const currentOperatorId = this.workflowActionService.getJointGraphWrapper().getCurrentHighlightedOperatorIDs()[0];
    const operatorType = this.workflowActionService.getTexeraGraph().getOperator(currentOperatorId).operatorType;

    if (operatorType === "RUDFSource" || operatorType === "RUDF") {
      this.changeLanguage("r");
    } else if (operatorType === "PythonUDFV2" || operatorType === "PythonUDFSourceV2" || operatorType === "DualInputPortsPythonUDFV2"
    ) {
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

        if(pythonConsoleUpdateEvent.messages.length === 0){
          return;
        }
        let lastMsg = pythonConsoleUpdateEvent.messages[pythonConsoleUpdateEvent.messages.length-1];
        if(lastMsg.title.startsWith("break")){
          this.lastBreakLine = Number(lastMsg.title.split(" ")[1]);
        }
        if(lastMsg.title.startsWith("*** Blank or comment")){
          this.isUpdatingBreakpoints = true;
          this.instance!["lineNumberAndDecorationIdMap"].forEach((v:string,k:number,m:Map<number, string>) =>{
            if(k === this.lastBreakLine){
              console.log("removing "+k)
              this.breakpointManager?.removeBreakpoint(k);
              this.instance!["removeSpecifyDecoration"](v, k);
            }
          })
          this.isUpdatingBreakpoints = false;
        }

      });

    this.editor.onContextMenu((e:EditorMouseEvent) =>{
      const { type, range, detail, position } = this.getMouseEventTarget(e);

      if (type === MouseTargetType.GUTTER_GLYPH_MARGIN) {
        // This indicates that the current position of the mouse is over the total number of lines in the editor
        if (detail.isAfterLines) {
          return;
        }

        // Get the layout info of the editor
        const layoutInfo = this.editor.getLayoutInfo();

        // Get the range start line number
        const startLineNumber = range.startLineNumber;

        if(!this.instance!["lineNumberAndDecorationIdMap"].has(startLineNumber)){
          return;
        }

        // Get the top position for the start line number
        const topForLineNumber = this.editor.getTopForLineNumber(startLineNumber);
        // Calculate the middle y position for the line number
        const lineHeight = this.editor.getOption(monaco.editor.EditorOption.lineHeight);
        const middleForLineNumber = topForLineNumber + lineHeight / 2;

        // Get the editor's DOM node and its bounding rect
        const editorDomNode = this.editor.getDomNode();
        const editorRect = editorDomNode.getBoundingClientRect();

        // Calculate x and y positions
        const x = editorRect.left + layoutInfo.glyphMarginLeft - this.editor.getScrollLeft();
        const y = editorRect.top + middleForLineNumber - this.editor.getScrollTop();

        this.showTooltip(x, y, startLineNumber, this.breakpointManager!);
      }
    })
  }

  private getMouseEventTarget(e: EditorMouseEvent) {
    return { ...(e.target as EditorMouseTarget) };
  }

  showTooltip(mouseX: number, mouseY: number, lineNum:number, breakpointManager:BreakpointManager): void {
    // Create tooltip element
    const tooltip = this.renderer.createElement("div");
    this.renderer.addClass(tooltip, "custom-tooltip");

    // Create header element
    const header = this.renderer.createElement("div");
    this.renderer.addClass(header, "tooltip-header");
    this.renderer.setProperty(header, "innerText", `Condition on line ${lineNum}:`);

    // Create textarea element
    const textarea = this.renderer.createElement("textarea");
    this.renderer.addClass(textarea, "custom-textarea");
    let oldCondition = breakpointManager.getCondition(lineNum)
    this.renderer.setProperty(textarea, "value", oldCondition ?? "");

    // Append header and textarea to tooltip
    this.renderer.appendChild(tooltip, header);
    this.renderer.appendChild(tooltip, textarea);

    // Append tooltip to the document body
    this.renderer.appendChild(document.body, tooltip);
    textarea.focus();
    // Function to remove the tooltip
    const removeTooltip = () => {
      const inputValue = textarea.value;
      if(inputValue != oldCondition){
        breakpointManager.setCondition(lineNum, inputValue);
      }
      if (removeTooltipListener) {
        removeTooltipListener();
      }
      if (removeFocusoutListener) {
        removeFocusoutListener();
      }
      // Add fade-out class
      this.renderer.addClass(tooltip, "fade-out");
      // Remove tooltip after the transition ends
      const transitionEndListener = this.renderer.listen(tooltip, "transitionend", () => {
        tooltip.remove();
        transitionEndListener();
      });
    };

    // Listen for Enter key press to exit edit mode
    const removeTooltipListener = this.renderer.listen(textarea, "keydown", (event: KeyboardEvent) => {
      if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        removeTooltip(); // Trigger fade-out and remove the tooltip
      }
    });

    // Listen for focusout event to remove the tooltip after 1 second
    const removeFocusoutListener = this.renderer.listen(textarea, "focusout", () => {
      setTimeout(removeTooltip, 300);
    });

    // Calculate tooltip dimensions after appending to the DOM
    const tooltipRect = tooltip.getBoundingClientRect();

    // Adjust the position to appear at the left side of the mouse
    const adjustedX = mouseX - tooltipRect.width - 10; // Subtracting width and adding some offset to the left
    const adjustedY = mouseY - tooltipRect.height / 2;

    // Update tooltip position
    this.renderer.setStyle(tooltip, "top", `${adjustedY}px`);
    this.renderer.setStyle(tooltip, "left", `${adjustedX}px`);
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

  private restoreBreakpoints(instance: MonacoBreakpoint, lineNums: string[]) {
    console.log("trying to restore " + lineNums);
    this.isUpdatingBreakpoints = true;
    instance["lineNumberAndDecorationIdMap"].forEach((v:string,k:number,m:Map<number, string>) =>{
      instance["removeSpecifyDecoration"](v, k);
    })
    for (let lineNumber of lineNums) {
        const range: monaco.IRange = {
          startLineNumber: Number(lineNumber),
          endLineNumber: Number(lineNumber),
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

    // Check if the AI provider is "openai"
    this.aiAssistantService
      .checkAIAssistantEnabled()
      .pipe(untilDestroyed(this))
      .subscribe({
        next: (isEnabled: string) => {
          if (isEnabled === "OpenAI") {
            // "Add Type Annotation" Button
            editor.addAction({
              id: "type-annotation-action",
              label: "Add Type Annotation",
              contextMenuGroupId: "1_modification",
              contextMenuOrder: 1.0,
              run: ed => {
                // User selected code (including range and content)
                const selection = ed.getSelection();
                const model = ed.getModel();
                if (!model || !selection) {
                  return;
                }
                // All the code in Python UDF
                const allcode = model.getValue();
                // Content of user selected code
                const code = model.getValueInRange(selection);
                // Start line of the selected code
                const lineNumber = selection.startLineNumber;
                this.handleTypeAnnotation(
                  code,
                  selection,
                  ed as monaco.editor.IStandaloneCodeEditor,
                  lineNumber,
                  allcode
                );
              },
            });
          }
        },
      });
    if (this.language == "python") {
      // this.connectLanguageServer();
    }

    this.instance = new MonacoBreakpoint({ editor });
    let createBreakpointDecoration = (
      range: Range,
      breakpointEnum: BreakpointEnum
    ): { options: editor.IModelDecorationOptions; range: Range } => {
      let condition = this.breakpointManager?.getCondition(range.startLineNumber);
      let isConditional = false;
      if (condition && condition !== "") {
        isConditional = true;
      }
      return {
        range,
        options:
          breakpointEnum === BreakpointEnum.Exist
            ? (isConditional ? CONDITIONAL_BREAKPOINT_OPTIONS : BREAKPOINT_OPTIONS)
            : BREAKPOINT_HOVER_OPTIONS,
      };
    };
    this.instance["createBreakpointDecoration"] = createBreakpointDecoration;
    this.instance["mouseDownDisposable"]?.dispose();
    this.instance["mouseDownDisposable"] = this.editor!.onMouseDown(
        (e: EditorMouseEvent) => {
          if(e.event.rightButton){
            return;
          }
          const model = this.editor.getModel();
          const { type, range, detail, position } = this.getMouseEventTarget(e);
          if (model && type === MouseTargetType.GUTTER_GLYPH_MARGIN) {
            // This indicates that the current position of the mouse is over the total number of lines in the editor
            if (detail.isAfterLines) {
              return;
            }

            const lineNumber = position.lineNumber;
            const decorationId =
                this.instance!["lineNumberAndDecorationIdMap"].get(lineNumber);

            /**
             * If a breakpoint exists on the current line,
             * it indicates that the current action is to remove the breakpoint
             */
            if (decorationId) {
              this.instance!["removeSpecifyDecoration"](decorationId, lineNumber);
            } else {
              this.instance!["createSpecifyDecoration"](range);
            }
          }
        }
    );




    (this.instance).on("breakpointChanged", lineNums => {
      if(this.isUpdatingBreakpoints){
        return;
      }
      this.breakpointManager?.setBreakpoints(lineNums.map(n => String(n)));
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


  private handleTypeAnnotation(
    code: string,
    range: monaco.Range,
    editor: monaco.editor.IStandaloneCodeEditor,
    lineNumber: number,
    allcode: string
  ): void {
    this.aiAssistantService
      .getTypeAnnotations(code, lineNumber, allcode)
      .pipe(takeUntil(this.workflowVersionStreamSubject))
      .subscribe({
        next: (response: TypeAnnotationResponse) => {
          const choices = response.choices || [];
          if (choices.length > 0 && choices[0].message && choices[0].message.content) {
            this.currentSuggestion = choices[0].message.content.trim();
            this.currentCode = code;
            this.currentRange = range;

            const position = editor.getScrolledVisiblePosition(range.getStartPosition());
            if (position) {
              this.suggestionTop = position.top + 100;
              this.suggestionLeft = position.left + 100;
            }

            this.showAnnotationSuggestion = true;

            if (this.annotationSuggestion) {
              this.annotationSuggestion.code = this.currentCode;
              this.annotationSuggestion.suggestion = this.currentSuggestion;
              this.annotationSuggestion.top = this.suggestionTop;
              this.annotationSuggestion.left = this.suggestionLeft;
            }
          } else {
            console.error("Error: OpenAI response does not contain valid message content", response);
          }
        },
        error: (error: unknown) => {
          console.error("Error fetching type annotations:", error);
        },
      });
  }

  // Called when the user clicks the "accept" button
  public acceptCurrentAnnotation(): void {
    // Avoid accidental calls
    if (!this.showAnnotationSuggestion || !this.currentRange || !this.currentSuggestion) {
      return;
    }

    if (this.currentRange && this.currentSuggestion) {
      const selection = new monaco.Selection(
        this.currentRange.startLineNumber,
        this.currentRange.startColumn,
        this.currentRange.endLineNumber,
        this.currentRange.endColumn
      );
      this.insertTypeAnnotations(this.editor, selection, this.currentSuggestion);
    }
    // close the UI after adding the annotation
    this.showAnnotationSuggestion = false;
  }

  // Called when the user clicks the "decline" button
  public rejectCurrentAnnotation(): void {
    // Do nothing except for closing the UI
    this.showAnnotationSuggestion = false;
    this.currentCode = "";
    this.currentSuggestion = "";
  }

  // Add the type annotation into monaco editor
  private insertTypeAnnotations(
    editor: monaco.editor.IStandaloneCodeEditor,
    selection: monaco.Selection,
    annotations: string
  ) {
    const endLineNumber = selection.endLineNumber;
    const endColumn = selection.endColumn;
    const range = new monaco.Range(
      // Insert the content to the end of the selected code
      endLineNumber,
      endColumn,
      endLineNumber,
      endColumn
    );
    const text = `${annotations}`;
    const op = {
      range: range,
      text: text,
      forceMoveMarkers: true,
    };
    editor.executeEdits("add annotation", [op]);
  }

  // private connectLanguageServer() {
  //   if (this.languageServerSocket === undefined) {
  //     this.languageServerSocket = new WebSocket(getWebsocketUrl("/python-language-server", "3000"));
  //     this.languageServerSocket.onopen = () => {
  //       if (this.languageServerSocket !== undefined) {
  //         const socket = toSocket(this.languageServerSocket);
  //         const reader = new WebSocketMessageReader(socket);
  //         const writer = new WebSocketMessageWriter(socket);
  //         const languageClient = new MonacoLanguageClient({
  //           name: "Python UDF Language Client",
  //           clientOptions: {
  //             documentSelector: ["python"],
  //             errorHandler: {
  //               error: () => ({ action: ErrorAction.Continue }),
  //               closed: () => ({ action: CloseAction.Restart }),
  //             },
  //           },
  //           connectionProvider: { get: () => Promise.resolve({ reader, writer }) },
  //         });
  //         languageClient.start();
  //         reader.onClose(() => languageClient.stop());
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
