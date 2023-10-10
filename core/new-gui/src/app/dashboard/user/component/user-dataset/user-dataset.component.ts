import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {SearchResultsComponent} from "../search-results/search-results.component";
import {FiltersComponent} from "../filters/filters.component";
import {UserService} from "../../../../common/service/user/user.service";
import {WorkflowPersistService} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {FileSaverService} from "../../service/user-file/file-saver.service";
import {SearchService} from "../../service/search.service";

@UntilDestroy()
@Component({
  templateUrl: "user-dataset.component.html",
  styleUrls: ["user-dataset.component.scss"]
})
export class UserDatasetComponent implements AfterViewInit {
  ngAfterViewInit(): void {
  }

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

  constructor(
    private userService: UserService,
    private workflowPersistService: WorkflowPersistService,
    private notificationService: NotificationService,
    private modalService: NgbModal,
    private router: Router,
    private fileSaverService: FileSaverService,
    private searchService: SearchService
  ) {}
}
