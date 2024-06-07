import {Injectable} from "@angular/core";
import {BehaviorSubject, Subject} from "rxjs";
import {WorkflowWebsocketService} from "../workflow-websocket/workflow-websocket.service";
import {FrontendDebugCommand, UDFBreakpointInfo} from "../../types/workflow-websocket.interface";
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
      if(evt.messages.length === 0){
        return;
      }
      if(evt.messages[evt.messages.length-1].msgType.name === "DEBUGGER"){
        this.executionActive = true;
        this.sendCommand();
      }
    });
  }

  private hitLineNum: Subject<number> = new BehaviorSubject(0)
  private lineNumToBreakpointSubject: Subject<Map<number, UDFBreakpointInfo>> = new BehaviorSubject(new Map());
  private lineNumToBreakpointMapping:Map<number, UDFBreakpointInfo> = new Map();
  private debugCommandQueue: FrontendDebugCommand[] = [];
  private executionActive = false;

  private queueCommand(cmd: FrontendDebugCommand){
    if(cmd.command === "clear"){
      const breakCommandIdx = this.debugCommandQueue.findIndex(request => request.command === "break" && request.line == cmd.line);
      if (breakCommandIdx !== -1) {
        this.debugCommandQueue.splice(breakCommandIdx, 1);
        return;
      }
    } else if(cmd.command === "condition"){
      const breakCommandIdx = this.debugCommandQueue.findIndex(request => request.command === "break" && request.line == cmd.line);
      if (breakCommandIdx !== -1) {
        this.debugCommandQueue[breakCommandIdx] = {...this.debugCommandQueue[breakCommandIdx], condition: cmd.condition}
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
      this.queueCommand({operatorId: this.currentOperatorId, command:"break", line: k, breakpointId:0, condition: v.condition})
    });
    this.setHitLineNum(0);
  }


  public removeBreakpoint(lineNum: number){
    this.setBreakpoints(Array.from(this.lineNumToBreakpointMapping.keys()).filter(e => e!= lineNum), false)
  }

  public setBreakpoints(lineNum: number[], triggerClear:boolean = true){
    console.log("set breakpoint"+lineNum)
    let changed = false;
    let lineSet = new Set(lineNum);
    lineSet.forEach(line =>{
      if(!this.lineNumToBreakpointMapping.has(line)){
        changed = true;
        this.lineNumToBreakpointMapping.set(line, {breakpointId:undefined, condition:""})
        console.log("add break command")
        this.queueCommand({operatorId: this.currentOperatorId,
          command:"break",
          line:line,
          breakpointId:0,
          condition: ""})
      }
    })
    this.lineNumToBreakpointMapping.forEach((v,k,m) =>{
      if(!lineSet.has(k)){
        changed = true;
        if(triggerClear){
          this.queueCommand({operatorId: this.currentOperatorId,
            command:"clear",
            line:k,
            breakpointId:v.breakpointId ?? 0, condition:""});
        }
        m.delete(k)
      }
    })
    if(changed){
      this.lineNumToBreakpointSubject.next(this.lineNumToBreakpointMapping);
    }
  }

  public getCondition(lineNum:number):string{
    let info = this.lineNumToBreakpointMapping.get(lineNum)!
    return info.condition;
  }

  public setCondition(lineNum:number, condition:string){
    let info = this.lineNumToBreakpointMapping.get(lineNum)!
    this.lineNumToBreakpointMapping.set(lineNum,  {...info, condition:condition});
    this.queueCommand({operatorId: this.currentOperatorId,
      command:"condition",
      line:lineNum,
      breakpointId:info.breakpointId ?? 0,
      condition: condition})
  }

  public assignBreakpointId(lineNum: number, breakpointId: number): void {
    console.log("assign id to breakpoint"+lineNum+" "+breakpointId)
    let info = this.lineNumToBreakpointMapping.get(lineNum)!
    this.lineNumToBreakpointMapping.set(lineNum,  {...info, breakpointId:breakpointId})
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
