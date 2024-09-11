import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { Location } from "@angular/common";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-result",
  templateUrl: "hub-workflow-detail.component.html",
  styleUrls: ["hub-workflow-detail.component.scss"],
})
export class HubWorkflowDetailComponent {
  wid: string | null;
  clonedWorklowId: number | undefined;

  workflow = {
    name: "Example Workflow",
    createdBy: "John Doe",
    steps: [
      {
        name: "Step 1: Data Collection",
        description: "Collect necessary data from various sources.",
        status: "Completed",
      },
      {
        name: "Step 2: Data Analysis",
        description: "Analyze the collected data for insights.",
        status: "In Progress",
      },
      {
        name: "Step 3: Report Generation",
        description: "Generate reports based on the analysis.",
        status: "Not Started",
      },
      {
        name: "Step 4: Presentation",
        description: "Present the findings to stakeholders.",
        status: "Not Started",
      },
    ],
  };

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private hubWorkflowService: HubWorkflowService,
    private router: Router
  ) {
    this.wid = this.route.snapshot.queryParamMap.get("wid");
  }

  goBack(): void {
    this.location.back();
  }

  cloneWorkflow(): void {
    this.hubWorkflowService
      .cloneWorkflow(Number(this.wid))
      .pipe(untilDestroyed(this))
      .subscribe(newWid => {
        this.clonedWorklowId = newWid;
        this.router.navigate([`/workflow/${this.clonedWorklowId}`]);
      });
  }
}
