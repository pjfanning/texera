import { Component } from "@angular/core";
import { HubWorkflow } from "../../type/hub-workflow.interface";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { SearchService } from "../../../../dashboard/service/user/search.service";
import { SearchFilterParameters } from "../../../../dashboard/type/search-filter-parameters";
import { SortMethod } from "../../../../dashboard/type/sort-method";
import { SearchResult } from "../../../../dashboard/type/search-result";
import { DashboardWorkflow } from "../../../../dashboard/type/dashboard-workflow.interface";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-result",
  templateUrl: "hub-workflow-result.component.html",
  styleUrls: ["hub-workflow-result.component.scss"],
})
export class HubWorkflowResultComponent {
  listOfWorkflows: HubWorkflow[] = [];

  constructor(private searchService: SearchService) {
    const params: SearchFilterParameters = {
      createDateStart: null,
      createDateEnd: null,
      modifiedDateStart: null,
      modifiedDateEnd: null,
      owners: [],
      ids: [],
      operators: [],
      projectIds: [],
    };

    this.searchService.conditional_search([], params, 0, 100, "workflow", SortMethod.NameAsc, "public")
      .pipe(untilDestroyed(this))
      .subscribe((result: SearchResult) => {
        console.log("Search Result:", result);
        this.listOfWorkflows = result.results
          .filter(item => item.resourceType === "workflow" && item.workflow !== undefined)
          .map(item => this.convertToHubWorkflow(item.workflow!)); // 将 item.workflow 转换为 HubWorkflow
      });
  }

  private convertToHubWorkflow(dashboardWorkflow: DashboardWorkflow): HubWorkflow {
    return {
      name: dashboardWorkflow.workflow.name,
      description: dashboardWorkflow.workflow.description,
      wid: dashboardWorkflow.workflow.wid,
      content: dashboardWorkflow.workflow.content,
      creationTime: dashboardWorkflow.workflow.creationTime,
      lastModifiedTime: dashboardWorkflow.workflow.lastModifiedTime,
    };
  }
}
