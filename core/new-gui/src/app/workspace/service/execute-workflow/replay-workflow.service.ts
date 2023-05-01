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
  public historyStatus: Record<number, string> = {};
  private displayWorkflowReplay = new Subject<string>();
  public operatorInfo: string[][] = [];
  public selectedKey = -1;
  public plannerStrategies: string[] = ["No checkpoint", "Complete - all", "Complete - optimized", "Partial - greedy"];
  public selectedMode: String = this.plannerStrategies[0];
  public isReplaying = false;
  public replayTime = 0;
  public checkpointTime = 0;
  public replayTimeLimit = 5;

  constructor(private workflowWebsocketService: WorkflowWebsocketService, private notification: NotificationService) {
    workflowWebsocketService.subscribeToEvent("WorkflowInteractionHistoryEvent").subscribe(e => {
      e.history.forEach(x => {
        this.historyStatus[x] = "interaction";
      });
    });

    workflowWebsocketService.subscribeToEvent("WorkflowAdditionalOperatorInfoEvent").subscribe(e =>{
      this.operatorInfo = e.data.split("\n").map(row => row.split(":"));
    });

    workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(e => {
      if(e.state === ExecutionState.Initializing || e.state === ExecutionState.Aborted){
        this.historyStatus = {};
        this.selectedKey = -1;
        this.operatorInfo = [];
        this.isReplaying = false;
      }
    });

    workflowWebsocketService.subscribeToEvent("WorkflowCheckpointedEvent").subscribe(e => {
      Object.entries(e.checkpointed).forEach(entry => {
        this.historyStatus[Number(entry[0])] = entry[1]?"full":"partial";
      });
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

  public selectReplayPoint(key: number): void {
    if(key in this.historyStatus){
      if(!this.isReplaying && this.selectedMode != "") {
        this.selectedKey = key;
        this.isReplaying = true;
        this.replayTime = 0;
        this.workflowWebsocketService.send("WorkflowReplayRequest", {replayPos: key, plannerStrategy: this.selectedMode, replayTimeLimit: this.replayTimeLimit*1000});
        this.notification.info("replaying time point " + (key/1000) + "s");
      }else{
        this.notification.info("replaying in progress");
      }
    }
  }

  public clickButton():void {
    this.workflowWebsocketService.send("WorkflowAdditionalOperatorInfoRequest", {});
  }

  public clickButton2():void {
    this.workflowWebsocketService.send("WorkflowCheckpointRequest", {});
  }
}
