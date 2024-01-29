import {DashboardEnvironment, DatasetOfEnvironmentDetails, Environment} from "../../../../dashboard/user/type/environment";
import {ActivatedRoute, Router} from "@angular/router";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {WorkflowPersistService} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {NgbdModalEnvironmentDatasetAddComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-environment-dataset-add/ngbd-modal-environment-dataset-add.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {WorkflowActionService} from "../../../service/workflow-graph/model/workflow-action.service";
import {DatasetVersionFileTreeNode} from "../../../../common/type/datasetVersionFileTree";
import {DatasetService} from "../../../../dashboard/user/service/user-dataset/dataset.service";

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

    // [did, dvid] => [DatasetOfEnvironmentDetails, DatasetVersionFileTreeNode[]]
    datasetsOfEnvironment: Map<number, [DatasetOfEnvironmentDetails, DatasetVersionFileTreeNode[]]> = new Map();
    datasetFileTrees: [number, string, DatasetVersionFileTreeNode[]][] = [];
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

    onClickOpenLinkDatasetWindow() {
        const modalRef = this.modalService.open(NgbdModalEnvironmentDatasetAddComponent);
        modalRef.result.then((result) => {
            if (result) {
                const did = Number(result)
                if (!isNaN(did) && this.eid) {
                    this.environmentService.addDatasetToEnvironment(this.eid, did)
                        .subscribe({
                            next: response => {
                                this.notificationService.success(`Add dataset ${did} to the environment succeed!`)
                                this.loadDatasetsOfEnvironment();
                            },
                            error: err => {
                                this.notificationService.error("Add dataset to environment encountering error.");
                            }
                        })
                }
            }
        }, (reason) => {
            this.notificationService.error("Add dataset to environment encountering error.")
        });
    }
}
