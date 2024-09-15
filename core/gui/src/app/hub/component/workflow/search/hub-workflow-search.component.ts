import { Component, Input, ViewChild } from "@angular/core";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { SearchResultsComponent } from "../../../../dashboard/component/user/search-results/search-results.component";
import { FiltersComponent } from "../../../../dashboard/component/user/filters/filters.component";
import { SortMethod } from "../../../../dashboard/type/sort-method";
import { UserService } from "../../../../common/service/user/user.service";
import { WorkflowPersistService } from "../../../../common/service/workflow-persist/workflow-persist.service";
import { UserProjectService } from "../../../../dashboard/service/user/project/user-project.service";
import { NotificationService } from "../../../../common/service/notification/notification.service";
import { NzModalService } from "ng-zorro-antd/modal";
import { Router } from "@angular/router";
import { FileSaverService } from "../../../../dashboard/service/user/file/file-saver.service";
import { SearchService } from "../../../../dashboard/service/user/search.service";
import { isDefined } from "../../../../common/util/predicate";
import { firstValueFrom } from "rxjs";
import { DashboardEntry, UserInfo } from "../../../../dashboard/type/dashboard-entry";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-search",
  templateUrl: "hub-workflow-search.component.html",
  styleUrls: ["hub-workflow-search.component.scss"],
})
export class HubWorkflowSearchComponent {
  private _searchResultsComponent?: SearchResultsComponent;
  @ViewChild(SearchResultsComponent) get searchResultsComponent(): SearchResultsComponent {
    if (this._searchResultsComponent) {
      return this._searchResultsComponent;
    }
    throw new Error("Property cannot be accessed before it is initialized.");
  }
  set searchResultsComponent(value: SearchResultsComponent) {
    this._searchResultsComponent = value;
  }
  private _filters?: FiltersComponent;
  @ViewChild(FiltersComponent) get filters(): FiltersComponent {
    if (this._filters) {
      return this._filters;
    }
    throw new Error("Property cannot be accessed before it is initialized.");
  }
  set filters(value: FiltersComponent) {
    value.masterFilterListChange.pipe(untilDestroyed(this)).subscribe({ next: () => this.search() });
    this._filters = value;
  }
  private masterFilterList: ReadonlyArray<string> | null = null;

  @Input() public pid?: number = undefined;
  @Input() public accessLevel?: string = undefined;
  public sortMethod = SortMethod.EditTimeDesc;
  lastSortMethod: SortMethod | null = null;

  constructor(
    private userService: UserService,
    private searchService: SearchService
  ) {}

  ngAfterViewInit() {
    this.userService
      .userChanged()
      .pipe(untilDestroyed(this))
      .subscribe(() => this.search());
  }


  /**
   * Searches workflows with keywords and filters given in the masterFilterList.
   * @returns
   */
  async search(forced: Boolean = false): Promise<void> {
    const sameList =
      this.masterFilterList !== null &&
      this.filters.masterFilterList.length === this.masterFilterList.length &&
      this.filters.masterFilterList.every((v, i) => v === this.masterFilterList![i]);
    if (!forced && sameList && this.sortMethod === this.lastSortMethod) {
      // If the filter lists are the same, do no make the same request again.
      return;
    }
    this.lastSortMethod = this.sortMethod;
    this.masterFilterList = this.filters.masterFilterList;
    let filterParams = this.filters.getSearchFilterParameters();
    if (isDefined(this.pid)) {
      // force the project id in the search query to be the current pid.
      filterParams.projectIds = [this.pid];
    }
    this.searchResultsComponent.reset(async (start, count) => {
      const results = await firstValueFrom(
        this.searchService.publicSearch(
          [""],
          filterParams,
          start,
          count,
          "workflow",
          this.sortMethod
        )
      );

      const userIds = new Set<number>();
      results.results.forEach(i => {
        if (i.workflow && i.workflow.ownerId) {
          userIds.add(i.workflow.ownerId);
        }
      });

      let userIdToInfoMap: { [key: number]: UserInfo } = {};
      if (userIds.size > 0) {
        userIdToInfoMap = await firstValueFrom(this.searchService.getUserInfo(Array.from(userIds)));
      }

      return {
        entries: results.results.map(i => {
          if (i.workflow) {
            const entry = new DashboardEntry(i.workflow);

            const userInfo = userIdToInfoMap[i.workflow.ownerId];
            if (userInfo) {
              entry.setOwnerName(userInfo.userName);
              entry.setOwnerGoogleAvatar(userInfo.googleAvatar ?? "");
            }
            return entry;
          } else {
            throw new Error("Unexpected type in SearchResult.");
          }
        }),
        more: results.more,
      };
    });
    await this.searchResultsComponent.loadMore();
  }
}
