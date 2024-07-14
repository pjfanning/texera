import { Component } from "@angular/core";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { HubWorkflow } from "../../type/hub-workflow.interface";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-result",
  templateUrl: "hub-workflow-detail.component.html",
  styleUrls: ["hub-workflow-detail.component.scss"],
})
export class HubWorkflowDetailComponent {
  listOfWorkflows: HubWorkflow[] = [];
  constructor(private hubWorkflowService: HubWorkflowService) {
    hubWorkflowService
      .getWorkflowList()
      .pipe(untilDestroyed(this))
      .subscribe(workflows => {
        this.listOfWorkflows = workflows;
      });
  }
}
