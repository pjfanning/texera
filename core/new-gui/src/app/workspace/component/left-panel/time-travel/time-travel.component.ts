import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { WorkflowActionService } from "../../../service/workflow-graph/model/workflow-action.service";
import { WorkflowExecutionsEntry } from "../../../../dashboard/user/type/workflow-executions-entry";
import { TimeTravelService } from "../../../service/time-travel/time-travel.service";
import { ExecuteWorkflowService } from "../../../service/execute-workflow/execute-workflow.service";
import { WorkflowVersionService } from "../../../../dashboard/user/service/workflow-version/workflow-version.service";

const FULL_REPLAY_FLAG = "Full Replay";
@UntilDestroy()
@Component({
  selector: "texera-time-travel",
  templateUrl: "time-travel.component.html",
  styleUrls: ["time-travel.component.scss"],
})
export class TimeTravelComponent implements OnInit {
  interactionHistories: { [eid: number]: string[] } = {};
  public executionList: WorkflowExecutionsEntry[] = [];
  expandedRows = new Set<number>(); // Tracks expanded rows by execution ID
  private reverted = false;

  public wid: number | undefined;

  constructor(
    private workflowActionService: WorkflowActionService,
    public timetravelService: TimeTravelService,
    public executeWorkflowService: ExecuteWorkflowService,
    private workflowVersionService: WorkflowVersionService
  ) {}

  ngOnInit(): void {
    this.wid = this.workflowActionService.getWorkflowMetadata()?.wid;
    if (this.wid === undefined) {
      return;
    }
    // gets the versions result and updates the workflow versions table displayed on the form
    this.displayExecutionWithLogs(this.wid);
  }

  toggleRow(eId: number): void {
    if (this.expandedRows.has(eId)) {
      this.expandedRows.delete(eId);
    } else {
      this.expandedRows.add(eId);
      this.getInteractionHistory(eId); // Call only if needed
    }
  }

  getInteractionHistory(eid: number): void {
    if (this.wid === undefined) {
      return;
    }
    this.timetravelService
      .retrieveInteractionHistory(this.wid, eid)
      .pipe(untilDestroyed(this))
      .subscribe(data => {
        this.interactionHistories[eid] = data; // TODO:add FULL_REPLAY here to support fault tolerance.
      });
  }

  /**
   * calls the http get request service to display the versions result in the table
   */
  displayExecutionWithLogs(wid: number): void {
    this.timetravelService
      .retrieveLoggedExecutions(wid)
      .pipe(untilDestroyed(this))
      .subscribe(executions => {
        this.executionList = executions;
      });
  }

  closeFrame() {
    if (this.reverted) {
      this.workflowVersionService.closeReadonlyWorkflowDisplay();
      try {
        this.executeWorkflowService.killWorkflow();
      } catch (e) {
        // ignore exception.
      }
    }
    this.timetravelService.closeFrame();
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
