import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, Output} from "@angular/core";
import {DashboardEntry} from "../../../type/dashboard-entry";
import {Workflow} from "../../../../../common/type/workflow";
import {Dataset} from "../../../../../common/type/dataset";
import {DashboardProject} from "../../../type/dashboard-project.interface";
import {environment} from "../../../../../../environments/environment";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
    DEFAULT_WORKFLOW_NAME,
    WorkflowPersistService,
} from "../../../../../common/service/workflow-persist/workflow-persist.service";
import {FileSaverService} from "../../../service/user-file/file-saver.service";
import {UserProjectService} from "../../../service/user-project/user-project.service";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {NgbdModalWorkflowExecutionsComponent} from "../../user-workflow/ngbd-modal-workflow-executions/ngbd-modal-workflow-executions.component";
import {firstValueFrom} from "rxjs";
import {ShareAccessComponent} from "../../share-access/share-access.component";
import {DashboardDataset} from "../../../type/dashboard-dataset.interface";
import {NotificationService} from "../../../../../common/service/notification/notification.service";

@UntilDestroy()
@Component({
    selector: "texera-user-dataset-list-item",
    templateUrl: "./user-dataset-list-item.component.html",
    styleUrls: ["./user-dataset-list-item.component.scss"],
})
export class UserDatasetListItemComponent {
    ROUTER_DATASET_BASE_URL = "/dashboard/dataset";

    private _entry?: DashboardEntry;

    @Output()
    refresh = new EventEmitter<void>();

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

    get dataset(): Dataset {
        if (!this.entry.dataset) {
            throw new Error(
                "Incorrect type of DashboardEntry provided to UserDatasetListItemComponent. Entry must be dataset."
            );
        }
        return this.entry.dataset.dataset;
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
        private datasetService: DatasetService,
        private notificationService: NotificationService) {
    }

    public confirmUpdateDatasetCustomName(name: string) {
        if (this.entry.dataset.dataset.name == name) {
            return;
        }

        if (this.entry.dataset.dataset.did)
            this.datasetService
                .updateDatasetName(this.entry.dataset.dataset.did, name)
                .pipe(untilDestroyed(this))
                .subscribe({
                    next: () => {
                        this.entry.dataset.dataset.name = name;
                        this.editingName = false;
                    },
                    error: () => {
                        this.notificationService.error("Update dataset name failed");
                        this.editingName = false;
                    },
                })
    }

    public confirmUpdateDatasetCustomDescription(description: string) {
        if (this.entry.dataset.dataset.description == description) {
            return;
        }

        if (this.entry.dataset.dataset.did)
            this.datasetService
                .updateDatasetDescription(this.entry.dataset.dataset.did, description)
                .pipe(untilDestroyed(this))
                .subscribe({
                    next: () => {
                        this.entry.dataset.dataset.description = description;
                        this.editingDescription = false;
                    },
                    error: () => {
                        this.notificationService.error("Update dataset description failed");
                        this.editingDescription = false;
                    },
                })
    }

    public onClickGetDatasetVersions() {
    }

    public onClickOpenShareAccess() {
        const modalRef = this.modalService.open(ShareAccessComponent);
        console.log(this.entry.dataset);
        modalRef.componentInstance.writeAccess = this.entry.dataset.accessPrivilege === "WRITE";
        modalRef.componentInstance.type = "dataset";
        modalRef.componentInstance.id = this.dataset.did;
        modalRef.closed.pipe(untilDestroyed(this)).subscribe(_ => {
            this.refresh.emit();
        });
    }
}
