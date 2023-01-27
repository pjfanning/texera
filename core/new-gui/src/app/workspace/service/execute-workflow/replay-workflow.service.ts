import { Injectable } from "@angular/core";
import { Observable, Subject } from "rxjs";
import { NotificationService } from "src/app/common/service/notification/notification.service";
import { WorkflowWebsocketService } from "../workflow-websocket/workflow-websocket.service";
import { ExecutionState } from "../../types/execute-workflow.interface";

export const DISPLAY_WORKFLOW_EXECUTION_REPLAY = "display_workflow_execution_replay";

@Injectable({
  providedIn: "root",
})
export class ReplayWorkflowService {
  public history: readonly number[] = [];
  private displayWorkflowReplay = new Subject<string>();
  public operatorInfo: string = "";
  public selectedIndex = -1;
  
  public replayStarted = false;
  public replayEnded = false;

  constructor(private workflowWebsocketService: WorkflowWebsocketService, private notification: NotificationService) {
    workflowWebsocketService.subscribeToEvent("WorkflowInteractionHistoryEvent").subscribe(e => {
      this.history = e.history;
    });

    workflowWebsocketService.subscribeToEvent("WorkflowAdditionalOperatorInfoEvent").subscribe(e =>{
      this.operatorInfo = e.data;
    });
    this.history = [1,5, 20, 30, 40]

    workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(e => {
      if (e.state === ExecutionState.Paused || e.state === ExecutionState.Completed) {
        if (this.replayStarted) {
          this.replayStarted = false;
          this.replayEnded = true;
        }
      }
    })
  }

  public displayWorkflowReplayPanel(): void {
    this.displayWorkflowReplay.next(DISPLAY_WORKFLOW_EXECUTION_REPLAY);
  }

  public displayWorkflowReplayStream(): Observable<string> {
    return this.displayWorkflowReplay.asObservable();
  }

  public selectReplayPoint(index: number): void {
    this.selectedIndex = index;
    this.replayStarted = true;
    this.replayEnded = false;
    this.workflowWebsocketService.send("WorkflowReplayRequest", { replayPos: index });
    this.notification.info("replaying time point " + this.history[index] + "s");
  }

  public clickButton():void {
    this.workflowWebsocketService.send("WorkflowAdditionalOperatorInfoRequest", {});
  }
}
