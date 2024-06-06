import { AfterViewInit, Component, ComponentRef, ElementRef, OnDestroy, ViewChild } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { WorkflowActionService } from "../../service/workflow-graph/model/workflow-action.service";
import { WorkflowVersionService } from "src/app/dashboard/user/service/workflow-version/workflow-version.service";
import { YText } from "yjs/dist/src/types/YText";
import { MonacoBinding } from "y-monaco";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
// import { MonacoLanguageClient } from "monaco-languageclient";
import { toSocket, WebSocketMessageReader, WebSocketMessageWriter } from "vscode-ws-jsonrpc";
import { CoeditorPresenceService } from "../../service/workflow-graph/model/coeditor-presence.service";
import { DomSanitizer, SafeStyle } from "@angular/platform-browser";
import { Coeditor } from "../../../common/type/user";
import { YType } from "../../types/shared-editing.interface";
import { isUndefined } from "lodash";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api.js";
import { FormControl } from "@angular/forms";
import { MonacoBreakpoint } from "monaco-breakpoints";
import { WorkflowWebsocketService } from "../../service/workflow-websocket/workflow-websocket.service";
import { ExecuteWorkflowService } from "../../service/execute-workflow/execute-workflow.service";
import { BreakpointManager, UdfDebugService } from "../../service/operator-debug/udf-debug.service";
import { isDefined } from "../../../common/util/predicate";
import { ConsoleUpdateEvent } from "../../types/workflow-common.interface";

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

    const instance = new MonacoBreakpoint({ editor });

    instance.on("breakpointChanged", lineNums => {
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
        instance.removeHighlight();
        if (lineNum != 0) {
          instance.setLineHighlight(lineNum);
        }
      });
    this.breakpointManager?.getLineNumToBreakpointMappingStream().pipe(untilDestroyed(this)).subscribe(
      mapping =>{
        console.log("trigger"+mapping)
        this.restoreBreakpoints(instance, Array.from(mapping.keys()));
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
}
