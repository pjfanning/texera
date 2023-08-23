import {Injectable} from "@angular/core";
import {Observable, Subject} from "rxjs";
import {NotificationService} from "src/app/common/service/notification/notification.service";
import {WorkflowWebsocketService} from "../workflow-websocket/workflow-websocket.service";
import {ExecutionState} from "../../types/execute-workflow.interface";

export const DISPLAY_WORKFLOW_EXECUTION_REPLAY = "display_workflow_execution_replay";

export class HistoryNode {
  public isInteraction: boolean;
  public checkpointStatus:string;
  public padding:number;
  public isStartingPoint:boolean;
  constructor(isInteraction:boolean, checkpointStatus:string, padding:number, isStartingPoint:boolean){
    this.isInteraction = isInteraction;
    this.checkpointStatus = checkpointStatus;
    this.padding = padding;
    this.isStartingPoint = isStartingPoint;
  }
}


@Injectable({
  providedIn: "root",
})
export class ReplayWorkflowService {
  public history: Map<number, HistoryNode> = new Map();
  private displayWorkflowReplay = new Subject<string>();
  public operatorInfo: string[][] = [];
  public selectedKey = -1;
  public plannerStrategies: string[] = ["No checkpoint", "Complete - all", "Complete - optimized", "Partial - greedy"];
  public selectedMode: String = this.plannerStrategies[0];
  public isReplaying = false;
  public replayTime = 0;
  public checkpointTime = 0;
  public replayTimeLimit = 5;
  public triggeredPrepPhase = false;
  public prepPhaseFinished = false;
  public globalcheckpoints = false;

  public triggerPrepPhase(){
    this.triggeredPrepPhase = true;
    this.workflowWebsocketService.send("WorkflowReplayRequest",{replayPos: -1, plannerStrategy: this.globalcheckpoints?"global":"partial", replayTimeLimit: this.replayTimeLimit*1000});
  }

  constructor(private workflowWebsocketService: WorkflowWebsocketService, private notification: NotificationService) {
    workflowWebsocketService.subscribeToEvent("WorkflowInteractionHistoryEvent").subscribe(e => {
      if(e.history.length > 0){
        this.history.clear();
        let pxRange = 500;
        let entireRange = e.history[e.history.length-1] - e.history[0];
        let padding = 0;
        let last = -1;
        for (let i = 0; i < e.history.length; i++) {
          if(e.checkpointStatus[i] !== "none" || e.isInteraction[i]){
            this.history.set(e.history[i], new HistoryNode(e.isInteraction[i], e.checkpointStatus[i], 0, i == 0));
            padding = 0;
            last = i;
          }
          if(i != e.history.length-1){
            padding += ((e.history[i+1] - e.history[i])/entireRange)*pxRange;
          }
          if(last != -1){
            this.history.get(e.history[last])!.padding = padding;
          }
        }
      }
    });

    workflowWebsocketService.subscribeToEvent("WorkflowAdditionalOperatorInfoEvent").subscribe(e =>{
      this.operatorInfo = e.data.split("\n").map(row => row.split(":"));
    });

    workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(e => {
      if(e.state === ExecutionState.Initializing || e.state === ExecutionState.Aborted){
        this.history = new Map();
        this.selectedKey = -1;
        this.operatorInfo = [];
        this.isReplaying = false;
      }
    });

    workflowWebsocketService.subscribeToEvent("WorkflowReplayCompletedEvent").subscribe(e => {
      this.prepPhaseFinished = true;
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
    if(this.history.get(key)!.isInteraction){
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
