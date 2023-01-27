import { Injectable } from "@angular/core";
import { Observable, Subject } from "rxjs";
import { NotificationService } from "src/app/common/service/notification/notification.service";
import { WorkflowWebsocketService } from "../workflow-websocket/workflow-websocket.service";

export const DISPLAY_WORKFLOW_EXECUTION_REPLAY = "display_workflow_execution_replay";

@Injectable({
  providedIn: "root",
})
export class ReplayWorkflowService {
  public history: readonly number[] = [];
  private displayWorkflowReplay = new Subject<string>();
  public operatorInfo: string = "";

  constructor(private workflowWebsocketService: WorkflowWebsocketService, private notification: NotificationService) {
    workflowWebsocketService.subscribeToEvent("WorkflowInteractionHistoryEvent").subscribe(e => {
      this.history = e.history;
    });

    workflowWebsocketService.subscribeToEvent("WorkflowAdditionalOperatorInfoEvent").subscribe(e =>{
      this.operatorInfo = e.data;
    });
  }

  public displayWorkflowReplayPanel(): void {
    this.displayWorkflowReplay.next(DISPLAY_WORKFLOW_EXECUTION_REPLAY);
  }

  public displayWorkflowReplayStream(): Observable<string> {
    return this.displayWorkflowReplay.asObservable();
  }

  public selectReplayPoint(index: number): void {
    this.workflowWebsocketService.send("WorkflowReplayRequest", { replayPos: index });
    this.notification.info("replaying time point " + this.history[index] + "s");
  }

  public clickButton():void {
    this.workflowWebsocketService.send("WorkflowAdditionalOperatorInfoRequest", {});
  }
}
