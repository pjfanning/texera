import {DashboardEnvironment, DatasetOfEnvironmentDetails, Environment} from "../../../../dashboard/user/type/environment";
import {ActivatedRoute, Router} from "@angular/router";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {WorkflowPersistService} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {WorkflowActionService} from "../../../service/workflow-graph/model/workflow-action.service";
import {DatasetVersionFileTreeNode} from "../../../../common/type/datasetVersionFileTree";
import {DatasetService} from "../../../../dashboard/user/service/user-dataset/dataset.service";
import {DashboardDataset} from "../../../../dashboard/user/type/dashboard-dataset.interface";

@UntilDestroy()
@Component({
    selector: "texera-environment",
    templateUrl: "environment.component.html",
    styleUrls: ["environment.component.scss"],
})
export class EnvironmentComponent implements OnInit {
    @Output()
    environment: EventEmitter<Environment> = new EventEmitter<Environment>();

    @Input()
    eid : number | undefined;

    wid: number | undefined;

    selectedMenu: "datasets" | "metadata" = "datasets";

    // [did] => [DatasetOfEnvironmentDetails, DatasetVersionFileTreeNode[]]
    datasetsOfEnvironment: Map<number, [DatasetOfEnvironmentDetails, DatasetVersionFileTreeNode[]]> = new Map();
    datasetFileTrees: [number, string, DatasetVersionFileTreeNode[]][] = [];

    // dataset link related control
    showDatasetLinkModal: boolean = false;
    userAccessibleDatasets: DashboardDataset[] = [];
    filteredLinkingDatasetsName: string[] = [];
    inputDatasetName?: string;

    constructor(
        private router: Router,
        private activatedRoute : ActivatedRoute,
        private environmentService: EnvironmentService,
        private notificationService: NotificationService,
        private workflowPersistService: WorkflowPersistService,
        private workflowActionService: WorkflowActionService,
        private datasetService: DatasetService,
        private modalService: NgbModal) {}

    ngOnInit(): void {
        // initilize the environment info
        if (this.eid) {
            // used by the environment editor directly
            this.environmentService.retrieveEnvironmentByEid(this.eid)
                .pipe(untilDestroyed(this))
                .subscribe({
                    next: env => {
                        this.environment.emit(env.environment);
                        this.loadDatasetsOfEnvironment();
                    },
                    error: err => {
                        this.notificationService.error(`Runtime environment loading error!`)
                    }
                })
        } else {
            this.wid = this.workflowActionService.getWorkflowMetadata()?.wid;
            if (this.wid) {
                // use wid to fetch the eid first
                this.workflowPersistService.retrieveWorkflowEnvironment(this.wid)
                    .pipe(untilDestroyed(this))
                    .subscribe({
                        next: env => {
                            this.environment.emit(env);
                            this.eid = env.eid;
                            this.loadDatasetsOfEnvironment();
                        },
                        error: err => {
                            this.notificationService.error(`Runtime environment loading error!`)
                        }
                    })
            }
        }
    }

    private loadDatasetsOfEnvironment() {
        if (this.eid) {
            const eid = this.eid;
            this.environmentService.retrieveDatasetsOfEnvironmentDetails(eid)
                .subscribe({
                    next: datasets => {
                        datasets.forEach((entry) => {
                            const did = entry.dataset.did;
                            const dvid = entry.version.dvid;
                            if (did && dvid) {
                                this.datasetService.retrieveDatasetVersionFileTree(did, dvid)
                                    .subscribe({
                                        next: datasetFileTree => {
                                            this.datasetsOfEnvironment.set(did, [entry, datasetFileTree]);
                                            this.datasetFileTrees.push([did, entry.dataset.name, datasetFileTree])
                                        }
                                    })
                            }
                        })
                    },
                    error: err => {
                        this.notificationService.error("Datasets of Environment loading error!");
                    }
                })
        }
    }

    onClickOpenEnvironmentDatasetDetails(did: number) {
        // TODO: show the datasets of environment details.
    }

    // related control for dataset link modal
    retrieveUserAccessibleUnlinkedDatasets() {
        // initialize the datasets info
        this.datasetService.retrieveAccessibleDatasets()
            .subscribe({
                next: datasets => {
                    this.userAccessibleDatasets =
                        datasets.filter(ds => {
                            const newDid = ds.dataset.did;
                            return !newDid || this.datasetsOfEnvironment.has(newDid);
                        });
                }
            })
    }
    onClickOpenLinkDatasetWindow() {
        if (this.selectedMenu == 'datasets') {
            this.retrieveUserAccessibleUnlinkedDatasets();
            if (this.userAccessibleDatasets.length == 0) {
                this.notificationService.warning(`There is no available datasets to be linked to the environment.`)
            } else {
                this.showDatasetLinkModal = true;
            }
        }
    }

    handleConfirmLinkDataset() {
        this.showDatasetLinkModal = false;
    }

    handleCancelLinkDataset() {
        this.showDatasetLinkModal = false;
    }

    onUserInputDatasetName(event: Event): void {
        const value = this.inputDatasetName;

        if (value) {
            this.filteredLinkingDatasetsName = this.userAccessibleDatasets
                .map(d => d.dataset.name)
                .filter(name => name.toLowerCase().includes(value.toLowerCase()));
        }

        // console.log(this.filteredLinkingDatasetsName)
    }

}
