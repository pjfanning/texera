import { Component, OnInit } from "@angular/core";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { ActivatedRoute, Router } from "@angular/router";
import { SearchService} from "../../../../dashboard/service/user/search.service";
import { SearchFilterParameters } from "../../../../dashboard/type/search-filter-parameters";
import { SortMethod } from "../../../../dashboard/type/sort-method";
import { SearchResult } from "../../../../dashboard/type/search-result";
import { HubWorkflow } from "../../type/hub-workflow.interface";
import { DashboardWorkflow } from "../../../../dashboard/type/dashboard-workflow.interface";
import { UserService } from "src/app/common/service/user/user.service";


@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-search",
  templateUrl: "hub-workflow-search.component.html",
  styleUrls: ["hub-workflow-search.component.scss"],
})
export class HubWorkflowSearchComponent implements OnInit{
  workflowCount: number | undefined;
  listOfWorkflows: HubWorkflow[] = [];
  hasResults: boolean = false;
  public isLoggedIn: boolean = false;
  constructor(private hubWorkflowService: HubWorkflowService,
              private router: Router,
              private searchService: SearchService,
              private activateRouter: ActivatedRoute,
              private userService: UserService,
  ) {
    this.isLoggedIn = this.userService.isLogin();
  }

  ngOnInit() {
    this.hubWorkflowService
      .getWorkflowCount()
      .pipe(untilDestroyed(this))
      .subscribe(count => {
        this.workflowCount = count;
      });

    this.searchWorkflows("");
  }

  searchWorkflows(query: string): void {
    // eslint-disable-next-line rxjs-angular/prefer-takeuntil
    this.activateRouter.queryParams.subscribe(queryParams => {
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

      this.searchService.search([query], params, 0, 4, "workflow", SortMethod.EditTimeDesc)
        .pipe(untilDestroyed(this))
        .subscribe((result: SearchResult) => {
          console.log("Search Result:", result);
          this.listOfWorkflows = result.results
            .filter(item => item.resourceType === "workflow" && item.workflow !== undefined)
            .map(item => this.convertToHubWorkflow(item.workflow!));
          this.hasResults = this.listOfWorkflows.length > 0;
        });
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


  navigateToSearchResult(): void {
    this.router.navigate(["/dashboard/hub/workflow/search/result"], { queryParams: { q: "" } });
  }
}
