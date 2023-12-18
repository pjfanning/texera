import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { WorkflowActionService } from "../../../service/workflow-graph/model/workflow-action.service";
import {WorkflowExecutionsEntry} from "../../../../dashboard/user/type/workflow-executions-entry";
import {Observable, of, ReplaySubject} from "rxjs";
import {TimeTravelService} from "../../../service/time-travel/time-travel.service";

@UntilDestroy()
@Component({
  selector: "texera-time-travel",
  templateUrl: "time-travel.component.html",
  styleUrls: ["time-travel.component.scss"],
})
export class TimeTravelComponent implements OnInit {


  entries: number[] = [123, 456, 789]; // Example array of eids
  interactionHistories: { [eid: number]: String[] } = {};


  public executionList: WorkflowExecutionsEntry[] = [];

  public wid: number | undefined;

  constructor(
    private workflowActionService: WorkflowActionService,
    public timetravelService:TimeTravelService,
  ) {}

  ngOnInit(): void {
    this.wid = this.workflowActionService.getWorkflowMetadata()?.wid;
    if (this.wid === undefined) {
      return;
    }
    // gets the versions result and updates the workflow versions table displayed on the form
    this.displayExecutionWithLogs(this.wid);
  }

  getInteractionHistory(eid: number):void {
    if (this.wid === undefined) {
      return;
    }
    this.timetravelService.retrieveInteractionHistory(this.wid, eid).pipe(untilDestroyed(this)).subscribe(
      data => {
        this.interactionHistories[eid] = ["a","b","c"];
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
        this.executionList = executions
      });
  }
}
