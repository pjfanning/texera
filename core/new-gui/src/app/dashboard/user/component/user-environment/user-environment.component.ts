import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {SearchResultsComponent} from "../search-results/search-results.component";
import {FiltersComponent} from "../filters/filters.component";
import {UserService} from "../../../../common/service/user/user.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {SearchService} from "../../service/search.service";
import {firstValueFrom} from "rxjs";
import {DashboardEntry} from "../../type/dashboard-entry";
import {SortMethod} from "../../type/sort-method";
import {NgbdModalEnvironmentAddComponent} from "./ngbd-modal-environment-add/ngbd-modal-environment-add.component";
import {DashboardEnvironment, Environment} from "../../type/environment";
import {EnvironmentService} from "../../service/user-environment/environment.service";
import next from "ajv/dist/vocabularies/next";

export const ROUTER_ENVIRONMENT_VIEW_URL = "/"

@UntilDestroy()
@Component({
    selector: "texera-environment-section",
    templateUrl: "user-environment.component.html",
    styleUrls: ["user-environment.component.scss"]
})
export class UserEnvironmentComponent implements AfterViewInit {

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
        value.masterFilterListChange.pipe(untilDestroyed(this)).subscribe({next: () => this.search()});
        this._filters = value;
    }

    private masterFilterList: ReadonlyArray<string> | null = null;

    public sortMethod = SortMethod.NameAsc;
    lastSortMethod: SortMethod | null = null;

    // dummy vars
    constructor(
        private userService: UserService,
        private notificationService: NotificationService,
        private modalService: NgbModal,
        private router: Router,
        private searchService: SearchService,
        private environmentService: EnvironmentService
    ) {
    }

    ngAfterViewInit() {
        this.reloadDashboardEnvironmentEntries()
    }

    /**
     * Searches environment with keywords and filters given in the masterFilterList.
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
                    "environment",
                    this.sortMethod
                )
            );
            return {
                entries: results.results.map(i => {
                    if (i.environment) {
                        return new DashboardEntry(i.environment);
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
     * open a new environment. will redirect to a pre-emptied environment view space
     */
    public onClickOpenEnvironmentFromDashboard(): void {
    }

    public onClickOpenEnvironmentAddComponent(): void {
        const modalRef = this.modalService.open(NgbdModalEnvironmentAddComponent)

        // When the modal is closed, retrieve the result
        modalRef.result.then(
            (new_entry: Environment) => {
                // Do something with the new_entry here
                // this.environments.unshift(entry);
                // console.log(this.environments);
                // this.searchResultsComponent.setEntries(this.environments);
                this.environmentService.addEnvironment(new_entry).subscribe({
                        next(_) {
                            console.log("environment creation successfully")

                        },
                        error(err) {
                            console.error('Something wrong occurred: ' + err);
                        }
                    }
                );
                },
        );

        modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {
            this.reloadDashboardEnvironmentEntries(true)
        });
    }

    public reloadDashboardEnvironmentEntries(forced: boolean = false): void {
        this.userService
            .userChanged()
            .pipe(untilDestroyed(this))
            .subscribe(() => this.search(forced));
    }

    public deleteEnvironment(event: DashboardEntry) {

    }
}
