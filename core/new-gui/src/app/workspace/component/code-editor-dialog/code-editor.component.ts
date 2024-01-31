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
import { ConsoleMessage, ConsoleUpdateEvent } from "../../types/workflow-common.interface";
import { RingBuffer } from "ring-buffer-ts";
import { CONSOLE_BUFFER_SIZE } from "../../service/workflow-console/workflow-console.service";


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


  constructor(
    private sanitizer: DomSanitizer,
    private workflowActionService: WorkflowActionService,
    private workflowVersionService: WorkflowVersionService,
    public coeditorPresenceService: CoeditorPresenceService,
    public executeWorkflowService: ExecuteWorkflowService,
    public workflowWebsocketService: WorkflowWebsocketService,
    public udfDebugService: UdfDebugService,
  ) {
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

    this.breakpointManager = this.udfDebugService.initManager(this.operatorID);

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
          this.breakpointManager?.addBreakpoint(lineNumber, breakpoint);
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
    for (let lineNumber of lineNums) {
      const decorationId = instance["lineNumberAndDecorationIdMap"].get(lineNumber);
      if (decorationId) {
        instance["removeSpecifyDecoration"](decorationId, lineNumber);
      } else {
        const range: monaco.IRange = {
          startLineNumber: lineNumber,
          endLineNumber: lineNumber,
          startColumn: 0,
          endColumn: 0,
        };
        instance["createSpecifyDecoration"](range);
      }
    }
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
      language: "python",
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
    // this.connectLanguageServer();

    const instance = new MonacoBreakpoint({ editor });

    const currentOperatorId: string = this.workflowActionService
      .getJointGraphWrapper()
      .getCurrentHighlightedOperatorIDs()[0];
    const workerIds = this.executeWorkflowService.getWorkerIds(currentOperatorId);

    this.restoreBreakpoints(instance, this.breakpointManager?.getLinesWithBreakpoint()!);

    instance.on("breakpointChanged", lineNums => {
      const existingLines = this.breakpointManager?.getLinesWithBreakpoint();
      existingLines?.filter(number => !lineNums.includes(number)).forEach(lineNum => {
        console.log("trying to remove line " + lineNum);
        const breakpointId = this.breakpointManager?.getBreakpoint(lineNum);
        for (let worker of workerIds) {
          this.workflowWebsocketService.send("DebugCommandRequest", {
            operatorId: currentOperatorId,
            workerId: worker,
            cmd: "clear " + breakpointId,
          });
        }
        this.breakpointManager?.removeBreakpoint(lineNum);
      })

      for (let lineNum of lineNums) {
        if (!this.breakpointManager?.hasBreakpoint(lineNum)) {
          for (let worker of workerIds) {
            this.workflowWebsocketService.send("DebugCommandRequest", {
              operatorId: currentOperatorId,
              workerId: worker,
              cmd: "break " + lineNum,
            });
          }
        }
      }


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
