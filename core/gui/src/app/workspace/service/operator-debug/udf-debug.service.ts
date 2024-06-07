import {Injectable} from "@angular/core";
import {BehaviorSubject, Subject} from "rxjs";
import {WorkflowWebsocketService} from "../workflow-websocket/workflow-websocket.service";
import {FrontendDebugCommand} from "../../types/workflow-websocket.interface";
import {ExecutionState} from "../../types/execute-workflow.interface";

export class BreakpointManager {

  constructor(private workflowWebsocketService:WorkflowWebsocketService,
              private currentOperatorId:string) {
    workflowWebsocketService.subscribeToEvent("WorkflowStateEvent").subscribe(evt =>{
      if(evt.state === ExecutionState.Initializing){
        this.sendCommand();
      }
    })

    workflowWebsocketService.subscribeToEvent("ConsoleUpdateEvent").subscribe(evt =>{
      if(evt.messages[evt.messages.length-1].msgType.name === "DEBUGGER"){
        this.executionActive = true;
        this.sendCommand();
      }
    });
  }

  private hitLineNum: Subject<number> = new BehaviorSubject(0)
  private lineNumToBreakpointSubject: Subject<Map<number, number | undefined>> = new BehaviorSubject(new Map());
  private lineNumToBreakpointMapping:Map<number, number | undefined> = new Map();
  private debugCommandQueue: FrontendDebugCommand[] = [];
  private executionActive = false;

  private queueCommand(cmd: FrontendDebugCommand){
    if(cmd.command !== "break"){
      const breakCommandIdx = this.debugCommandQueue.findIndex(request => request.command === "break" && request.line == cmd.line);
      if (breakCommandIdx !== -1) {
        this.debugCommandQueue.splice(breakCommandIdx, 1);
        return;
      }
    }
    this.debugCommandQueue.push(cmd);
    if(this.executionActive){
      this.sendCommand();
    }
  }

  private sendCommand(){
    if(this.debugCommandQueue.length > 0){
      let payload = this.debugCommandQueue.shift();
      this.workflowWebsocketService.prepareDebugCommand(payload!);
      let needContinue = this.debugCommandQueue.length === 0 || this.debugCommandQueue[this.debugCommandQueue.length-1].command !== "continue";
      if(payload?.command === "break" && needContinue){
        this.debugCommandQueue.push({...payload, command: "continue"});
      }
    }
  }

  public getBreakpointHitStream() {
    return this.hitLineNum.asObservable();
  }

  public getLineNumToBreakpointMappingStream(){
    return this.lineNumToBreakpointSubject.asObservable();
  }

  public resetState(){
    this.executionActive = false;
    this.debugCommandQueue = [];
    this.lineNumToBreakpointMapping.forEach((v,k) => {
      this.queueCommand({operatorId: this.currentOperatorId, command:"break", line: k, breakpointId:0})
    });
    this.setHitLineNum(0);
  }

  public setBreakpoints(lineNum: number[]){
    console.log("set breakpoint"+lineNum)
    let changed = false;
    let lineSet = new Set(lineNum);
    lineSet.forEach(line =>{
      if(!this.lineNumToBreakpointMapping.has(line)){
        changed = true;
        this.lineNumToBreakpointMapping.set(line, undefined)
        this.queueCommand({operatorId: this.currentOperatorId,
          command:"break",
          line:line,
          breakpointId:0})
      }
    })
    this.lineNumToBreakpointMapping.forEach((v,k,m) =>{
      if(!lineSet.has(k)){
        changed = true;
        this.queueCommand({operatorId: this.currentOperatorId,
          command:"clear",
          line:k,
          breakpointId:v ?? 0});
        m.delete(k)
      }
    })
    if(changed){
      this.lineNumToBreakpointSubject.next(this.lineNumToBreakpointMapping);
    }
  }

  public assignBreakpointId(lineNum: number, breakpointId: number): void {
    console.log("assign id to breakpoint"+lineNum+" "+breakpointId)
    this.lineNumToBreakpointMapping.set(lineNum, breakpointId)
  }

  public setHitLineNum(lineNum: number) {
    this.hitLineNum.next(lineNum)
  }
}

@Injectable({
  providedIn: "root",
})
export class UdfDebugService {
  private breakpointManagers: Map<string, BreakpointManager> = new Map();

  constructor(private workflowWebsocketService: WorkflowWebsocketService) {
    console.log("udf debug service started")
    this.subscribePythonLineHighlight();
  }

  public getOrCreateManager(operatorId: string): BreakpointManager {
    if (!this.breakpointManagers.has(operatorId)) {
      this.breakpointManagers.set(operatorId, new BreakpointManager(this.workflowWebsocketService, operatorId));
    }
    return this.breakpointManagers.get(operatorId)!;
  }

  public getAllManagers():BreakpointManager[]{
    return Array.from(this.breakpointManagers.values());
  }

  private subscribePythonLineHighlight() {
    this.workflowWebsocketService.subscribeToEvent("ConsoleUpdateEvent").subscribe(event => {
      if (event.messages.length == 0) {
        return
      }
      const msg = event.messages[0]
      console.log("processing message", msg)
      const breakpointManager = this.getOrCreateManager(event.operatorId);
      if (msg.source == "(Pdb)" && msg.msgType.name == "DEBUGGER") {
        if (msg.title.startsWith(">")) {
          const lineNum = this.extractLineNumber(msg.title)
          if (lineNum) {
            breakpointManager.setHitLineNum(lineNum)
          }
        }
      }
      if (msg.msgType.name == "ERROR") {
        const lineNum = this.extractLineNumberException(msg.source)
        console.log(lineNum)
        if (lineNum) {
          breakpointManager.setHitLineNum(lineNum)
        }
      }
    })
  }

  private extractLineNumber(message: string): number | undefined {
    const regex = /\.py\((\d+)\)/;
    const match = message.match(regex);

    if (match && match[1]) {
      return parseInt(match[1]);
    }

    return undefined;
  }

  private extractLineNumberException(message: string): number | undefined {
    const regex = /:(\d+)/;
    const match = message.match(regex);

    if (match && match[1]) {
      return parseInt(match[1]);
    }

    return undefined;
  }
}
