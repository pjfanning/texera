import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { UserService } from "../../../../common/service/user/user.service";
import { Router } from "@angular/router";
import { SearchService } from "../../../service/user/search.service";
import { DatasetService } from "../../../service/user/dataset/dataset.service";
import { DashboardDataset } from "../../../type/dashboard-dataset.interface";
import Fuse from "fuse.js";
import { SearchFilterParameters } from "../../../type/search-filter-parameters";
import { SortMethod } from "../../../type/sort-method";
import { SearchResult } from "../../../type/search-result";
import { DashboardEntry } from "../../../type/dashboard-entry";
import { SearchResultsComponent } from "../search-results/search-results.component";
import { FiltersComponent } from "../filters/filters.component";
import { isDefined } from "../../../../common/util/predicate";
import { firstValueFrom } from "rxjs";

@UntilDestroy()
@Component({
  selector: "texera-dataset-section",
  templateUrl: "user-dataset.component.html",
  styleUrls: ["user-dataset.component.scss"],
})
export class UserDatasetComponent implements AfterViewInit {
  // public dashboardUserDatasetEntries: ReadonlyArray<DashboardDataset> = [];
  // public userDatasetSearchValue: string = "";
  // public filteredDatasetNames: Array<string> = [];
  // public isTyping: boolean = false;
  // public fuse = new Fuse([] as ReadonlyArray<DashboardDataset>, {
  //   shouldSort: true,
  //   threshold: 0.2,
  //   location: 0,
  //   distance: 100,
  //   minMatchCharLength: 1,
  //   keys: ["dataset.name"],
  // });

  public sortMethod = SortMethod.EditTimeDesc;
  lastSortMethod: SortMethod | null = null;
  @ViewChild(SearchResultsComponent) searchResultsComponent?: SearchResultsComponent;

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

  constructor(
    private userService: UserService,
    private router: Router,
    private searchService: SearchService,
    // private datasetService: DatasetService,
  ) {}

  ngAfterViewInit() {
    this.userService
      .userChanged()
      .pipe(untilDestroyed(this))
      .subscribe(() => this.search());
  }

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
    if (!this.searchResultsComponent) {
      throw new Error("searchResultsComponent is undefined.");
    }
    let filterParams = this.filters.getSearchFilterParameters();
    this.searchResultsComponent.reset(async (start, count) => {
      const results = await firstValueFrom(
        this.searchService.search(
          this.filters.getSearchKeywords(),
          filterParams,
          start,
          count,
          "dataset",
          this.sortMethod
        )
      );
      return {
        entries: results.results.map(i => {
          if (i.dataset) {
            return new DashboardEntry(i.dataset);
          } else {
            throw new Error("Unexpected type in SearchResult.");
          }
        }),
        more: results.more,
      };
    });
    await this.searchResultsComponent.loadMore();
  }

  public onClickOpenDatasetAddComponent(): void {
    this.router.navigate(["/dashboard/dataset/create"]);
  }

  // selectedMenu: "All Datasets" | "Your Datasets" | "Shared with you" | "Public Datasets" = "All Datasets";

  // ngOnInit() {
  //   this.reloadDashboardDatasetEntries();
  // }

  //
  // public searchInputOnChange(value: string): void {
  //   this.isTyping = true;
  //   this.filteredDatasetNames = [];
  //   const datasetArray = this.dashboardUserDatasetEntries;
  //   datasetArray.forEach(datasetEntry => {
  //     if (datasetEntry.dataset.name.toLowerCase().indexOf(value.toLowerCase()) !== -1) {
  //       this.filteredDatasetNames.push(datasetEntry.dataset.name);
  //     }
  //   });
  // }
  //
  // private reloadDashboardDatasetEntries(): void {
  //   this.datasetService
  //     .retrieveAccessibleDatasets()
  //     .pipe(untilDestroyed(this))
  //     .subscribe(response => {
  //       this.dashboardUserDatasetEntries = response.datasets;
  //     });
  // }
  //
  // public getDatasetArray(): ReadonlyArray<DashboardDataset> {
  //   const datasetArray = this.dashboardUserDatasetEntries;
  //   let resultDatasetArray: DashboardDataset[] = [];
  //   if (!datasetArray) {
  //     return [];
  //   } else if (this.userDatasetSearchValue !== "" && !this.isTyping) {
  //     this.fuse.setCollection(datasetArray);
  //     resultDatasetArray = this.fuse.search(this.userDatasetSearchValue).map(item => {
  //       return item.item;
  //     });
  //   } else if (!this.isTyping) {
  //     resultDatasetArray = datasetArray.slice();
  //   }
  //   // apply the filter condition
  //   if (this.selectedMenu === "Your Datasets") {
  //     resultDatasetArray = resultDatasetArray.filter(dataset => {
  //       return dataset.isOwner;
  //     });
  //   } else if (this.selectedMenu === "Shared with you") {
  //     resultDatasetArray = resultDatasetArray.filter(dataset => {
  //       return !dataset.isOwner && !dataset.dataset.isPublic;
  //     });
  //   } else if (this.selectedMenu === "Public Datasets") {
  //     resultDatasetArray = resultDatasetArray.filter(dataset => {
  //       return dataset.dataset.isPublic;
  //     });
  //   }
  //   return resultDatasetArray;
  // }
  //
  // public deleteDataset(entry: DashboardDataset) {
  //   if (entry.dataset.did) {
  //     this.datasetService
  //       .deleteDatasets([entry.dataset.did])
  //       .pipe(untilDestroyed(this))
  //       .subscribe(_ => {
  //         this.reloadDashboardDatasetEntries();
  //       });
  //   }
  // }
}
