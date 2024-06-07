import {Injectable} from "@angular/core";
import {interval, Observable, Subject, Subscription, timer} from "rxjs";
import {webSocket, WebSocketSubject} from "rxjs/webSocket";
import {
  FrontendDebugCommand,
  TexeraWebsocketEvent,
  TexeraWebsocketEventTypeMap,
  TexeraWebsocketEventTypes,
  TexeraWebsocketRequest,
  TexeraWebsocketRequestTypeMap,
  TexeraWebsocketRequestTypes,
} from "../../types/workflow-websocket.interface";
import {delayWhen, filter, map, retryWhen, tap} from "rxjs/operators";
import {environment} from "../../../../environments/environment";
import {AuthService} from "../../../common/service/user/auth.service";
import {getWebsocketUrl} from "src/app/common/util/url";
import {ExecutionState} from "../../types/execute-workflow.interface";

export const WS_HEARTBEAT_INTERVAL_MS = 10000;
export const WS_RECONNECT_INTERVAL_MS = 3000;

@Injectable({
  providedIn: "root",
})
export class WorkflowWebsocketService {
  private static readonly TEXERA_WEBSOCKET_ENDPOINT = "wsapi/workflow-websocket";

  public isConnected: boolean = false;
  public numWorkers: number = -1;

  private websocket?: WebSocketSubject<TexeraWebsocketEvent | TexeraWebsocketRequest>;
  private wsWithReconnectSubscription?: Subscription;
  private readonly webSocketResponseSubject: Subject<TexeraWebsocketEvent> = new Subject();
  private requestQueue: Array<FrontendDebugCommand> = [];
  private assignedWorkerIds: Map<string, readonly string[]> = new Map();

  constructor() {
    // setup heartbeat
    interval(WS_HEARTBEAT_INTERVAL_MS).subscribe(_ => this.send("HeartBeatRequest", {}));
  }

  public websocketEvent(): Observable<TexeraWebsocketEvent> {
    return this.webSocketResponseSubject;
  }

  /**
   * Subscribe to a particular type of workflow websocket event
   */
  public subscribeToEvent<T extends TexeraWebsocketEventTypes>(
    type: T
  ): Observable<{ type: T } & TexeraWebsocketEventTypeMap[T]> {
    return this.websocketEvent().pipe(
      filter(event => event.type === type),
      map(event => event as { type: T } & TexeraWebsocketEventTypeMap[T])
    );
  }

  public send<T extends TexeraWebsocketRequestTypes>(type: T, payload: TexeraWebsocketRequestTypeMap[T]): void {
    const request = {
      type,
      ...payload,
    } as any as TexeraWebsocketRequest;
    if(request.type === "WorkflowKillRequest"){
      this.assignedWorkerIds.clear();
    }
    this.websocket?.next(request);
  }

  public clearDebugCommands(){
    this.requestQueue = [];
  }

  public getWorkerIds(operatorId: string): ReadonlyArray<string> {
    return this.assignedWorkerIds.get(operatorId) || [];
  }


  public prepareDebugCommand(payload:FrontendDebugCommand){
    this.requestQueue.push(payload);
    this.processQueue();
  }

  private sendDebugCommandRequest(request: FrontendDebugCommand, workerId: string): void {
    let cmd: string = "";
    if(request.command === "break"){
      cmd = "break "+request.line;
    }else if(request.command === "clear"){
      cmd = "clear "+request.breakpointId;
    }else{
      cmd = "continue";
    }
    this.send("DebugCommandRequest", {
      operatorId: request.operatorId,
      workerId,
      cmd,
    });
  }

  private processQueue(): void {
    // Process the request queue
    let initialQueueLength = this.requestQueue.length;

    // Loop through the initial length of the queue to prevent infinite loops with continuously failing items
    for (let i = 0; i < initialQueueLength; i++) {
      const request = this.requestQueue.shift();
      if (request) {
        if (this.assignedWorkerIds.has(request.operatorId)) {
          const workerIds = this.assignedWorkerIds.get(request.operatorId);
          if (workerIds) {
            for (let workerId of workerIds) {
              this.sendDebugCommandRequest(request, workerId);
            }
          }
        } else {
          // If the condition is not met, push the request back to the end of the queue
          this.requestQueue.push(request);
        }
      }
    }
  }

  public closeWebsocket() {
    this.wsWithReconnectSubscription?.unsubscribe();
    this.websocket?.complete();
  }

  public openWebsocket(wId: number) {
    const websocketUrl =
      getWebsocketUrl(WorkflowWebsocketService.TEXERA_WEBSOCKET_ENDPOINT, "") +
      "?wid=" +
      wId +
      (environment.userSystemEnabled && AuthService.getAccessToken() !== null
        ? "&access-token=" + AuthService.getAccessToken()
        : "");
    this.websocket = webSocket<TexeraWebsocketEvent | TexeraWebsocketRequest>(websocketUrl);
    // setup reconnection logic
    const wsWithReconnect = this.websocket.pipe(
      retryWhen(errors =>
        errors.pipe(
          tap(_ => (this.isConnected = false)), // update connection status
          tap(_ =>
            console.log(`websocket connection lost, reconnecting in ${WS_RECONNECT_INTERVAL_MS / 1000} seconds`)
          ),
          delayWhen(_ => timer(WS_RECONNECT_INTERVAL_MS)), // reconnect after delay
          tap(_ => {
            this.send("RegisterWorkflowIdRequest", { workflowId: wId }); // re-register wid
            this.send("HeartBeatRequest", {}); // try to send heartbeat immediately after reconnect
          })
        )
      )
    );
    // set up event listener on re-connectable websocket observable
    this.wsWithReconnectSubscription = wsWithReconnect.subscribe(event =>
      this.webSocketResponseSubject.next(event as TexeraWebsocketEvent)
    );

    // send wid registration and recover frontend state
    this.send("RegisterWorkflowIdRequest", { workflowId: wId });

    // refresh connection status
    this.websocketEvent().subscribe(evt => {
      if (evt.type === "ClusterStatusUpdateEvent") {
        this.numWorkers = evt.numWorkers;
      }
      if(evt.type === "WorkflowStateEvent"){
        if(evt.state === ExecutionState.Completed || evt.state === ExecutionState.Killed || evt.state === ExecutionState.Failed){
          this.assignedWorkerIds.clear();
        }
      }
      if(evt.type === "WorkerAssignmentUpdateEvent"){
        this.assignedWorkerIds.set(evt.operatorId, evt.workerIds);
        this.processQueue();
      }
      this.isConnected = true;
    });
  }

  public reopenWebsocket(wId: number) {
    this.closeWebsocket();
    this.openWebsocket(wId);
  }
}
