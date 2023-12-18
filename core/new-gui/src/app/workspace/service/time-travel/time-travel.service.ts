import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, ReplaySubject, Subject } from "rxjs";
import {
  WORKFLOW_EXECUTIONS_API_BASE_URL,
  WorkflowExecutionsService
} from "../../../dashboard/user/service/workflow-executions/workflow-executions.service";
import {WorkflowExecutionsEntry} from "../../../dashboard/user/type/workflow-executions-entry";
import {filter, map} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";

export const OPEN_TIMETRAVEL_FRAME_EVENT = "open_time_travel_frame_event";
export const CLOSE_TIMETRAVEL_FRAME_EVENT = "close_time_travel_frame_event";
@Injectable({
  providedIn: "root",
})
export class TimeTravelService {
  private timetravelFrameSubject: ReplaySubject<string> = new ReplaySubject<string>(1);

  constructor(
    private workflowExecutionsService:WorkflowExecutionsService,
    private http: HttpClient
  ) {}


  public timetravelDisplayObservable(): Observable<string> {
    return this.timetravelFrameSubject.asObservable();
  }

  public displayTimeTravelFrame(){
    this.timetravelFrameSubject.next(OPEN_TIMETRAVEL_FRAME_EVENT);
  }

  public closeFrame(){
    this.timetravelFrameSubject.next(CLOSE_TIMETRAVEL_FRAME_EVENT);
  }

  public retrieveInteractionHistory(wid: number, eid:number): Observable<String[]> {
    return this.http.get<String[]>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/${wid}/${eid}`);
  }

  public retrieveLoggedExecutions(wid: number): Observable<WorkflowExecutionsEntry[]> {
    return this.workflowExecutionsService.retrieveWorkflowExecutions(wid).pipe(
      map(executionList => executionList.filter(execution => {
        return true //execution.logLocation ? execution.logLocation.length > 0: false
      })))
  }

}
