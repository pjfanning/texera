import { Injectable } from "@angular/core";
import { BehaviorSubject, Subject } from "rxjs";
import { WorkflowWebsocketService } from "../workflow-websocket/workflow-websocket.service";

export class BreakpointManager {
  private hitLineNum: Subject<number> = new BehaviorSubject(0)
  private lineNumToBreakpointMapping: Map<number, number> = new Map();


  public getBreakpointHitStream() {
    return this.hitLineNum.asObservable();
  }
  public addBreakpoint(lineNum: number, breakpointId: number): void {
    this.lineNumToBreakpointMapping.set(lineNum, breakpointId);
  }

  public hasBreakpoint(lineNum: number): boolean {
    return this.lineNumToBreakpointMapping.has(lineNum);
  }
  public getBreakpoint(lineNum: number) : number {
    return this.lineNumToBreakpointMapping.get(lineNum)!;
  }

  public removeBreakpoint(lineNum: number) : void {
    this.lineNumToBreakpointMapping.delete(lineNum);
  }


  public getLinesWithBreakpoint() {
    return Array.from(this.lineNumToBreakpointMapping.keys());
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
    this.subscribePythonLineHighlight();
  }

  public getManager(operatorId: string): BreakpointManager | undefined {
    return this.breakpointManagers.get(operatorId);
  }

  public initManager(operatorId: string): BreakpointManager {
    if (!this.breakpointManagers.has(operatorId)) {
      this.breakpointManagers.set(operatorId, new BreakpointManager());
    }
    return this.breakpointManagers.get(operatorId)!;
  }

  private subscribePythonLineHighlight() {
    this.workflowWebsocketService.subscribeToEvent("ConsoleUpdateEvent").subscribe(event => {
      if (event.messages.length == 0) {
        return
      }
      const msg = event.messages[0]
      console.log("processing message", msg)
      const breakpointManager = this.initManager(event.operatorId);
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

  resetAll() {
    this.breakpointManagers.clear();
  }
}
