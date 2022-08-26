import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { AppSettings } from "../../../common/app-setting";
import { HttpClient } from "@angular/common/http";
import { WorkflowExecutionsEntry } from "../../type/workflow-executions-entry";
import { Workflow } from "../../../common/type/workflow";
import { filter, map } from "rxjs/operators";
import { WorkflowUtilService } from "../../../workspace/service/workflow-graph/util/workflow-util.service";
import { WorkflowActionService } from "src/app/workspace/service/workflow-graph/model/workflow-action.service";
import { UndoRedoService } from "src/app/workspace/service/undo-redo/undo-redo.service";
import { Router } from "@angular/router";

export const WORKFLOW_EXECUTIONS_API_BASE_URL = `${AppSettings.getApiEndpoint()}/executions`;
export const WORKFLOW_VERSIONS_API_BASE_URL = `${AppSettings.getApiEndpoint()}/version`;

@Injectable({
  providedIn: "root",
})
export class WorkflowExecutionsService {
  constructor(
    private http: HttpClient,
    private workflowActionService: WorkflowActionService,
    private undoRedoService: UndoRedoService,
    private router: Router
  ) {}

  private displayParticularWorkflowExecution = new BehaviorSubject<boolean>(false);

  /**
   * retrieves a list of execution for a particular workflow from backend database
   */
  retrieveWorkflowExecutions(wid: number): Observable<WorkflowExecutionsEntry[]> {
    return this.http.get<WorkflowExecutionsEntry[]>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/${wid}`);
  }

  /**
   * retrieves the result table for a particular execution from mongo database
   */
  retrieveExecutionResultTable(wid: number, result: String): Observable<[number, string]> {
    return this.http.get<[number, string]>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/${wid}/${result}`);
  }

  setIsBookmarked(wid: number, eId: number, isBookmarked: boolean): Observable<Object> {
    return this.http.put(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/set_execution_bookmark`, {
      wid,
      eId,
      isBookmarked,
    });
  }

  deleteWorkflowExecutions(wid: number, eId: number): Observable<Object> {
    return this.http.put(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/delete_execution`, {
      wid,
      eId,
    });
  }

  updateWorkflowExecutionsName(wid: number | undefined, eId: number, executionName: string): Observable<Response> {
    return this.http.post<Response>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/update_execution_name`, {
      wid,
      eId,
      executionName,
    });
  }

  public retrieveWorkflowByExecution(wid: number, vid: number): Observable<Workflow> {
    return this.http.get<Workflow>(`${WORKFLOW_VERSIONS_API_BASE_URL}/${wid}/${vid}`).pipe(
      filter((workflow: Workflow) => workflow != null),
      map(WorkflowUtilService.parseWorkflowInfo)
    );
  }

  public displayWorkflowExecution(workflow: Workflow) {
    // disable the undo/redo service because reloading the workflow is considered an action
    this.undoRedoService.disableWorkFlowModification();
    // enable modification to reload workflow
    this.workflowActionService.enableWorkflowModification();
    // reload the read only workflow version on the paper
    this.workflowActionService.reloadWorkflow(workflow);
    // set display particular execution flag true
    this.setDisplayParticularExecution(true);
    // disable modifications because it is read only
    this.workflowActionService.disableWorkflowModification();
  }

  public getDisplayParticularExecutionStream(): Observable<boolean> {
    return this.displayParticularWorkflowExecution.asObservable();
  }

  public setDisplayParticularExecution(flag: boolean): void {
    this.displayParticularWorkflowExecution.next(flag);
  }

  public closeParticularExecutionDisplay(): void {
    this.setDisplayParticularExecution(false);
    this.router.navigate(["/dashboard/workflow"], {
      state: { name: this.workflowActionService.getWorkflow().name, wid: this.workflowActionService.getWorkflow().wid },
    });
  }
}
