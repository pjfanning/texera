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
import { WorkflowPersistService } from "src/app/common/service/workflow-persist/workflow-persist.service";
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
    private workflowPersistService: WorkflowPersistService,
    private router: Router
  ) {}

  private displayParticularWorkflowExecution = new BehaviorSubject<boolean>(false);

  /**
   * retrieves a list of execution for a particular workflow from backend database
   */
  retrieveWorkflowExecutions(wid: number): Observable<WorkflowExecutionsEntry[]> {
    return this.http.get<WorkflowExecutionsEntry[]>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/${wid}`);
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

  public displayWorkflowExecution(wid: number, vid: number): Observable<Workflow> {
    return this.http.get<Workflow>(`${WORKFLOW_VERSIONS_API_BASE_URL}/${wid}/${vid}`).pipe(
      filter((workflow: Workflow) => workflow != null),
      map(WorkflowUtilService.parseWorkflowInfo)
    );
  }

  public getDisplayParticularExecutionStream(): Observable<boolean> {
    return this.displayParticularWorkflowExecution.asObservable();
  }

  public setDisplayParticularExecution(flag: boolean): void {
    this.displayParticularWorkflowExecution.next(flag);
  }

  public closeParticularExecutionDisplay(): void {
    var wid = this.workflowActionService.getWorkflow().wid;
    // var workflow = this.workflowActionService.getWorkflow()
    this.workflowActionService.enableWorkflowModification();
    // but still disable redo and undo service to not capture swapping the workflows, because enabling modifictions automatically enables undo and redo
    this.undoRedoService.disableWorkFlowModification();
    // reload the old workflow don't persist anything
    this.workflowActionService.reloadWorkflow(this.workflowActionService.getTempWorkflow());
    // clear the temp workflow
    this.workflowActionService.resetTempWorkflow();
    // after reloading the workflow, we can enable the undoredo service
    this.undoRedoService.enableWorkFlowModification();
    this.workflowPersistService.setWorkflowPersistFlag(true);
    this.setDisplayParticularExecution(false);

    this.router.navigate([`/dashboard/workflow`], {state: { wid: wid }});
  }
}
