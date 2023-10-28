import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, Output} from "@angular/core";
import {DashboardEntry} from "../../../type/dashboard-entry";
import {Workflow} from "../../../../../common/type/workflow";
import {DashboardProject} from "../../../type/dashboard-project.interface";
import {environment} from "../../../../../../environments/environment";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DEFAULT_WORKFLOW_NAME, WorkflowPersistService} from "../../../../../common/service/workflow-persist/workflow-persist.service";
import {FileSaverService} from "../../../service/user-file/file-saver.service";
import {UserProjectService} from "../../../service/user-project/user-project.service";
import {NgbdModalWorkflowExecutionsComponent} from "../../user-workflow/ngbd-modal-workflow-executions/ngbd-modal-workflow-executions.component";
import {firstValueFrom} from "rxjs";
import {ShareAccessComponent} from "../../share-access/share-access.component";
import {Environment} from "../../../type/environment";

@UntilDestroy()
@Component({
  selector: "texera-user-environment-list-item",
  templateUrl: "./user-environment-list-item.component.html",
  styleUrls: ["./user-environment-list-item.component.scss"],
})
export class UserEnvironmentListItemComponent {
  ROUTER_ENVIRONMENT_BASE_URL = "/dashboard/user-environment";

  private _entry?: DashboardEntry;

  @Input()
  get entry(): DashboardEntry {
    if (!this._entry) {
      throw new Error("entry property must be provided to UserDatasetListItemComponent.");
    }
    return this._entry;
  }

  set entry(value: DashboardEntry) {
    this._entry = value;
  }

  get environment(): Environment {
    if (!this.entry.environment) {
      throw new Error(
        "Incorrect type of DashboardEntry provided to UserEnvironmentListItemComponent. Entry must be environment."
      );
    }
    return this.entry.environment.environment;
  }

  @Input() editable = false;
  @Output() deleted = new EventEmitter<void>();
  @Output() duplicated = new EventEmitter<void>();

  editingName = false;
  editingDescription = false;
  /** Whether tracking metadata information about versions is enabled. */
  datasetVersionsTrackingEnabled: boolean = true;
  datasetVersionTrackingEnabled: boolean = true;

  constructor(
    private modalService: NgbModal,
  ) {}



  public confirmUpdateDatasetCustomName(val: any) {

  }

  public confirmUpdateEnvironmentCustomDescription(val: any) {

  }

  public onClickDownloadDataset() {

  }

  public onClickGetDatasetVersions() {

  }

  public onClickOpenShareAccess() {

  }
}
