import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {SearchResultsComponent} from "../search-results/search-results.component";
import {FiltersComponent} from "../filters/filters.component";
import {UserService} from "../../../../common/service/user/user.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {SearchService} from "../../service/search.service";
import {DatasetService} from "../../service/user-dataset/dataset.service";
import {firstValueFrom} from "rxjs";
import {DashboardEntry} from "../../type/dashboard-entry";
import {SortMethod} from "../../type/sort-method";
import {NgbdModalDatasetAddComponent} from "./ngbd-modal-dataset-add/ngbd-modal-dataset-add.component";
import {parseHierarchyToNodes} from "../../../../common/type/datasetVersion";

export const ROUTER_DATASET_VIEW_URL = "/"
@UntilDestroy()
@Component({
  selector: "texera-dataset-section",
  templateUrl: "user-dataset.component.html",
  styleUrls: ["user-dataset.component.scss"]
})
export class UserDatasetComponent implements AfterViewInit {

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

  public sortMethod = SortMethod.NameAsc;
  lastSortMethod: SortMethod | null = null;

  constructor(
    private userService: UserService,
    private notificationService: NotificationService,
    private modalService: NgbModal,
    private router: Router,
    private datasetService: DatasetService,
    private searchService: SearchService
  ) {}

  public multiWorkflowsOperationButtonEnabled(): boolean {
    if (this._searchResultsComponent) {
      return this.searchResultsComponent?.entries.filter(i => i.checked).length > 0;
    } else {
      return false;
    }
  }

  ngAfterViewInit() {
    this.reloadDashboardDatasetEntries()
  }

  /**
   * Searches datasets with keywords and filters given in the masterFilterList.
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

  /**
   * open a new dataset. will redirect to a pre-emptied dataset view space
   */
  public onClickOpenDatasetFromDashboard(): void {
    // this.router.navigate([`${ROUTER_DATASET_VIEW_URL}`], { queryParams: { pid: this.pid } }).then(null);
  }

  public onClickOpenDatasetAddComponent(): void {
    const modalRef = this.modalService.open(NgbdModalDatasetAddComponent)

    modalRef.componentInstance.datasetAdded.pipe(untilDestroyed(this))
     .subscribe(() => {
     this.reloadDashboardDatasetEntries(true); // Refresh the dataset list when a new dataset is added
    });

    modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {
      this.reloadDashboardDatasetEntries(true) //maybe delete this two lines? did not work for refresh
    });
  }

  private reloadDashboardDatasetEntries(forced: boolean = false): void {
    this.userService
      .userChanged()
      .pipe(untilDestroyed(this))
      .subscribe(() => this.search(forced));
  }

  public deleteDataset(entry: DashboardEntry) {
    console.log("fdsa");
    if (entry.dataset.dataset.did == undefined) {
      console.log(1111);
      return;
    }
    this.datasetService
      .deleteDatasets([entry.dataset.dataset.did])
      .pipe(untilDestroyed(this))
      .subscribe(_ => {
        this.searchResultsComponent.entries = this.searchResultsComponent.entries.filter(
          datasetEntry => datasetEntry.dataset.dataset.did !== entry.dataset.dataset.did
        );
      });
  }
}
