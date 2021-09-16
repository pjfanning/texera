import { Injectable } from "@angular/core";
import { interval, Observable, Subject, timer } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import {
  TexeraWebsocketEvent,
  TexeraWebsocketEventTypeMap,
  TexeraWebsocketEventTypes,
  TexeraWebsocketRequest,
  TexeraWebsocketRequestTypeMap,
  TexeraWebsocketRequestTypes,
} from "../../types/workflow-websocket.interface";
import { delayWhen, filter, map, retryWhen, tap } from "rxjs/operators";
import { ExecuteWorkflowService } from "../execute-workflow/execute-workflow.service";
import { TourService } from "ngx-tour-ng-bootstrap";
import { WorkflowActionService } from "../workflow-graph/model/workflow-action.service";

export const WS_HEARTBEAT_INTERVAL_MS = 10000;
export const WS_RECONNECT_INTERVAL_MS = 3000;

@Injectable({
  providedIn: "root",
})
export class WorkflowWebsocketService {
  private static readonly TEXERA_WEBSOCKET_ENDPOINT = "wsapi/workflow-websocket";

  public isConnected: boolean = false;

  private currentWid: string;

  private readonly websocket: WebSocketSubject<TexeraWebsocketEvent | TexeraWebsocketRequest>;
  private readonly webSocketResponseSubject: Subject<TexeraWebsocketEvent> = new Subject();

  constructor(workflowActionService: WorkflowActionService) {
    this.websocket = webSocket<TexeraWebsocketEvent | TexeraWebsocketRequest>(
      WorkflowWebsocketService.getWorkflowWebsocketUrl()
    );

    // setup reconnection logic
    const wsWithReconnect = this.websocket.pipe(
      retryWhen(error =>
        error.pipe(
          tap(_ => (this.isConnected = false)), // update connection status
          tap(_ =>
            console.log(`websocket connection lost, reconnecting in ${WS_RECONNECT_INTERVAL_MS / 1000} seconds`)
          ),
          delayWhen(_ => timer(WS_RECONNECT_INTERVAL_MS)), // reconnect after delay
          tap(_ => {
            this.currentWid = String(workflowActionService.getWorkflowMetadata().wid ?? "undefined workflow");
            this.send("RegisterWIdRequest", { wId: this.currentWid, recoverFrontendState: false }); // re-register wid
            this.send("HeartBeatRequest", {}); // try to send heartbeat immediately after reconnect
          })
        )
      )
    );

    // set up heartbeat
    interval(WS_HEARTBEAT_INTERVAL_MS).subscribe(_ => this.send("HeartBeatRequest", {}));

    // refresh connection status
    this.websocketEvent().subscribe(_ => (this.isConnected = true));

    // set up event listener on re-connectable websocket observable
    wsWithReconnect.subscribe(event => this.webSocketResponseSubject.next(event as TexeraWebsocketEvent));

    // send wid registration and recover frontend state
    this.currentWid = String(workflowActionService.getWorkflowMetadata().wid ?? "undefined workflow");
    this.send("RegisterWIdRequest", { wId: this.currentWid, recoverFrontendState: true });

    // listen to workflow metadata changed event
    workflowActionService.workflowMetaDataChanged().subscribe(() => {
      const newWid = String(workflowActionService.getWorkflowMetadata().wid ?? "undefined workflow");
      if (newWid !== this.currentWid) {
        this.currentWid = newWid;
        this.send("RegisterWIdRequest", { wId: this.currentWid, recoverFrontendState: false });
      }
    });
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
    this.websocket.next(request);
  }

  public static getWorkflowWebsocketUrl(): string {
    const websocketUrl = new URL(WorkflowWebsocketService.TEXERA_WEBSOCKET_ENDPOINT, document.baseURI);
    // replace protocol, so that http -> ws, https -> wss
    websocketUrl.protocol = websocketUrl.protocol.replace("http", "ws");
    return websocketUrl.toString();
  }
}
