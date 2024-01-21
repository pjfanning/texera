import { Component, OnDestroy, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { WorkflowActionService } from "../../../service/workflow-graph/model/workflow-action.service";
import { WorkflowExecutionsEntry } from "../../../../dashboard/user/type/workflow-executions-entry";
import { ExecuteWorkflowService } from "../../../service/execute-workflow/execute-workflow.service";
import { WorkflowVersionService } from "../../../../dashboard/user/service/workflow-version/workflow-version.service";
import {
  WORKFLOW_EXECUTIONS_API_BASE_URL,
  WorkflowExecutionsService,
} from "../../../../dashboard/user/service/workflow-executions/workflow-executions.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";

const FULL_REPLAY_FLAG = "Full Replay";
@UntilDestroy()
@Component({
  selector: "texera-time-travel",
  templateUrl: "time-travel.component.html",
  styleUrls: ["time-travel.component.scss"],
})
export class TimeTravelComponent implements OnInit, OnDestroy {
  interactionHistories: { [eid: number]: string[] } = {};
  public executionList: WorkflowExecutionsEntry[] = [];
  expandedRows = new Set<number>(); // Tracks expanded rows by execution ID
  public reverted = false;
  public wid: number | undefined;
  private timer: NodeJS.Timer | undefined;

  constructor(
    private workflowActionService: WorkflowActionService,
    public executeWorkflowService: ExecuteWorkflowService,
    private workflowVersionService: WorkflowVersionService,
    private workflowExecutionsService: WorkflowExecutionsService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.wid = this.workflowActionService.getWorkflowMetadata()?.wid;
    if (this.wid === undefined) {
      return;
    }
    // gets the versions result and updates the workflow versions table displayed on the form
    let localWid = this.wid;
    this.displayExecutionWithLogs(localWid);
    this.timer = setInterval(() => {
      this.displayExecutionWithLogs(localWid);
    }, 5000); // trigger per 5 secs
  }

  ngOnDestroy() {
    if (this.timer !== undefined) {
      clearInterval(this.timer);
    }
    if (this.reverted) {
      this.workflowVersionService.closeReadonlyWorkflowDisplay();
      try {
        this.executeWorkflowService.killWorkflow();
      } catch (e) {
        // ignore exception.
      }
    }
  }

  toggleRow(eId: number): void {
    if (this.expandedRows.has(eId)) {
      this.expandedRows.delete(eId);
    } else {
      this.expandedRows.add(eId);
      this.getInteractionHistory(eId); // Call only if needed
    }
  }

  retrieveInteractionHistory(wid: number, eid: number): Observable<string[]> {
    return this.http.get<string[]>(`${WORKFLOW_EXECUTIONS_API_BASE_URL}/${wid}/interactions/${eid}`);
  }

  public retrieveLoggedExecutions(wid: number): Observable<WorkflowExecutionsEntry[]> {
    return this.workflowExecutionsService.retrieveWorkflowExecutions(wid).pipe(
      map(executionList =>
        executionList.filter(execution => {
          return execution.logLocation ? execution.logLocation.length > 0 : false;
        })
      )
    );
  }

  getInteractionHistory(eid: number): void {
    if (this.wid === undefined) {
      return;
    }
    this.retrieveInteractionHistory(this.wid, eid)
      .pipe(untilDestroyed(this))
      .subscribe(data => {
        this.interactionHistories[eid] = data; // TODO:add FULL_REPLAY here to support fault tolerance.
      });
  }

  /**
   * calls the http get request service to display the versions result in the table
   */
  displayExecutionWithLogs(wid: number): void {
    this.retrieveLoggedExecutions(wid)
      .pipe(untilDestroyed(this))
      .subscribe(executions => {
        this.executionList = executions;
      });
  }

  onInteractionClick(vid: number, eid: number, interaction: string) {
    this.workflowVersionService
      .retrieveWorkflowByVersion(this.wid!, vid)
      .pipe(untilDestroyed(this))
      .subscribe(workflow => {
        this.workflowVersionService.displayReadonlyWorkflow(workflow);
        this.reverted = true;
        this.executeWorkflowService.executeWorkflowAmberTexeraWithReplay({ eid: eid, interaction: interaction });
      });
  }
}
