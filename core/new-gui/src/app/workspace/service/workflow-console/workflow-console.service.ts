import { Injectable } from "@angular/core";
import { WorkflowWebsocketService } from "../workflow-websocket/workflow-websocket.service";
import { PythonConsoleUpdateInfo } from "../../types/workflow-common.interface";
import { Subject } from "rxjs";
import { Observable } from "rxjs";
import { RingBuffer } from "ring-buffer-ts";
import { ExecutionState } from "../../types/execute-workflow.interface";

export const CONSOLE_BUFFER_SIZE = 100;

@Injectable({
  providedIn: "root",
})
export class WorkflowConsoleService {
  private consoleMessages: Map<string, RingBuffer<string>> = new Map();
  private consoleMessagesUpdateStream = new Subject<void>();

  constructor(private workflowWebsocketService: WorkflowWebsocketService) {
    this.registerAutoClearConsoleMessages();
    this.registerPythonPrintEventHandler();
  }

  registerPythonPrintEventHandler() {
    this.workflowWebsocketService
      .subscribeToEvent("PythonConsoleUpdateEvent")
      .subscribe((pythonConsoleUpdateInfo: PythonConsoleUpdateInfo) => {
        const operatorID = pythonConsoleUpdateInfo.operatorID;
        const messages = this.consoleMessages.get(operatorID) || new RingBuffer<string>(CONSOLE_BUFFER_SIZE);
        messages.add(pythonConsoleUpdateInfo.message);
        this.consoleMessages.set(operatorID, messages);
        this.consoleMessagesUpdateStream.next();
      });
  }

  registerAutoClearConsoleMessages() {
    this.workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(event => {
      if (event.state === ExecutionState.Initializing) {
        this.consoleMessages.clear();
      }
    });
  }

  getConsoleMessages(operatorID: string): ReadonlyArray<string> | undefined {
    return this.consoleMessages.get(operatorID)?.toArray();
  }

  getConsoleMessageUpdateStream(): Observable<void> {
    return this.consoleMessagesUpdateStream.asObservable();
  }
}
