import { Injectable } from "@angular/core";
import { WorkflowWebsocketService } from "../workflow-websocket/workflow-websocket.service";
import { PythonWorkerConsoleMessage, PythonConsoleUpdateEvent } from "../../types/workflow-common.interface";
import { Subject } from "rxjs";
import { Observable } from "rxjs";
import { RingBuffer } from "ring-buffer-ts";
import { ExecutionState } from "../../types/execute-workflow.interface";

export const CONSOLE_BUFFER_SIZE = 100;

@Injectable({
  providedIn: "root",
})
export class WorkflowConsoleService {
  private consoleMessages: Map<string, RingBuffer<PythonWorkerConsoleMessage>> = new Map();
  private consoleMessagesUpdateStream = new Subject<void>();

  constructor(private workflowWebsocketService: WorkflowWebsocketService) {
    this.registerAutoClearConsoleMessages();
    this.registerPythonConsoleUpdateEventHandler();
  }

  registerPythonConsoleUpdateEventHandler() {
    this.workflowWebsocketService
      .subscribeToEvent("PythonConsoleUpdateEvent")
      .subscribe((pythonConsoleUpdateEvent: PythonConsoleUpdateEvent) => {
        const operatorId = pythonConsoleUpdateEvent.operatorId;
        const messages =
          this.consoleMessages.get(operatorId) || new RingBuffer<PythonWorkerConsoleMessage>(CONSOLE_BUFFER_SIZE);
        messages.add(...pythonConsoleUpdateEvent.messages);
        this.consoleMessages.set(operatorId, messages);
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

  getConsoleMessages(operatorID: string): ReadonlyArray<PythonWorkerConsoleMessage> | undefined {
    return this.consoleMessages.get(operatorID)?.toArray();
  }

  getConsoleMessageUpdateStream(): Observable<void> {
    return this.consoleMessagesUpdateStream.asObservable();
  }
}
