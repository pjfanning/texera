import { Component } from "@angular/core";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { Router } from "@angular/router";


@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-search",
  templateUrl: "hub-workflow-search.component.html",
  styleUrls: ["hub-workflow-search.component.scss"],
})
export class HubWorkflowSearchComponent {
  workflowCount: number | undefined;

  constructor(hubWorkflowService: HubWorkflowService, private router: Router) {
    hubWorkflowService
      .getWorkflowCount()
      .pipe(untilDestroyed(this))
      .subscribe(count => {
        this.workflowCount = count;
      });
  }

  navigateToSearchResult(): void {
    this.router.navigate(["/dashboard/hub/workflow/search/result"], { queryParams: { q: "" } });
  }
}
