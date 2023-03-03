import {Injectable} from "@angular/core";
import {Observable, Subject} from "rxjs";
import {NotificationService} from "src/app/common/service/notification/notification.service";
import {WorkflowWebsocketService} from "../workflow-websocket/workflow-websocket.service";
import {ExecutionState} from "../../types/execute-workflow.interface";

export const DISPLAY_WORKFLOW_EXECUTION_REPLAY = "display_workflow_execution_replay";

@Injectable({
  providedIn: "root",
})
export class ReplayWorkflowService {
  public history: readonly number[] = [];
  public checkpointed: readonly number[] = [];
  private displayWorkflowReplay = new Subject<string>();
  public operatorInfo: string[][] = [];
  public selectedIndex = -1;
  public plannerStrategies: string[] = ["No checkpoint", "Complete - all", "Complete - naive", "Complete - optimized", "Partial - naive", "Partial - optimized"];
  public selectedMode: String = this.plannerStrategies[0];
  public isReplaying = false;
  public replayTime = 0;
  public checkpointTime = 0;
  public replayTimeLimit = 5;

  constructor(private workflowWebsocketService: WorkflowWebsocketService, private notification: NotificationService) {
    workflowWebsocketService.subscribeToEvent("WorkflowInteractionHistoryEvent").subscribe(e => {
      this.history = e.history;
    });

    workflowWebsocketService.subscribeToEvent("WorkflowAdditionalOperatorInfoEvent").subscribe(e =>{
      this.operatorInfo = e.data.split("\n").map(row => row.split(":"));
    });
    this.history = [1,5, 20, 30, 40];

    workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(e => {
      if(e.state === ExecutionState.Initializing || e.state === ExecutionState.Aborted){
        this.history = [];
        this.checkpointed = [];
        this.selectedIndex = -1;
        this.operatorInfo = [];
        this.isReplaying = false;
      }
    });

    workflowWebsocketService.subscribeToEvent("WorkflowCheckpointedEvent").subscribe(e => {
      this.checkpointed = e.checkpointed;
    });

    workflowWebsocketService.subscribeToEvent("WorkflowReplayCompletedEvent").subscribe(e => {
      this.isReplaying = false;
      this.replayTime = e.replayTime;
      this.checkpointTime = e.checkpointTime;
    });
  }

  public displayWorkflowReplayPanel(): void {
    this.displayWorkflowReplay.next(DISPLAY_WORKFLOW_EXECUTION_REPLAY);
  }

  public displayWorkflowReplayStream(): Observable<string> {
    return this.displayWorkflowReplay.asObservable();
  }

  public selectReplayPoint(index: number): void {
    if(!this.isReplaying && this.selectedMode != "") {
      this.selectedIndex = index;
      this.isReplaying = true;
      this.replayTime = 0;
      this.workflowWebsocketService.send("WorkflowReplayRequest", {replayPos: index, plannerStrategy: this.selectedMode, replayTimeLimit: this.replayTimeLimit});
      this.notification.info("replaying time point " + this.history[index] + "s");
    }else{
      this.notification.info("replaying in progress");
    }
  }

  public clickButton():void {
    this.workflowWebsocketService.send("WorkflowAdditionalOperatorInfoRequest", {});
  }

  public clickButton2():void {
    this.workflowWebsocketService.send("WorkflowCheckpointRequest", {});
  }
}
