import {DashboardEnvironment, DatasetOfEnvironmentDetails} from "../../../../dashboard/user/type/environment";
import {ActivatedRoute, Router} from "@angular/router";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {WorkflowPersistService} from "../../../../common/service/workflow-persist/workflow-persist.service";
import {NgbdModalEnvironmentDatasetAddComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-environment-dataset-add/ngbd-modal-environment-dataset-add.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@UntilDestroy()
@Component({
    selector: "texera-environment",
    templateUrl: "environment.component.html",
    styleUrls: ["environment.component.scss"],
})
export class EnvironmentComponent implements OnInit {
    @Output()
    environment: EventEmitter<DashboardEnvironment> = new EventEmitter<DashboardEnvironment>();

    @Input()
    eid : number | undefined;

    @Input()
    wid: number | undefined;

    selectedMenu: "datasets" = "datasets";

    datasetsOfEnvironment: Map<[number, number], DatasetOfEnvironmentDetails> = new Map();
    constructor(
        private router: Router,
        private activatedRoute : ActivatedRoute,
        private environmentService: EnvironmentService,
        private notificationService: NotificationService,
        private workflowPersistService: WorkflowPersistService,
        private modalService: NgbModal) {}

    ngOnInit(): void {
        if (this.eid) {
            // used by the environment editor directly
            this.environmentService.retrieveEnvironmentByEid(this.eid)
                .pipe(untilDestroyed(this))
                .subscribe({
                    next: env => {
                        this.environment.emit(env);
                        this.loadDatasetsOfEnvironment();
                    },
                    error: err => {
                        this.notificationService.error(`Runtime environment loading error!`)
                    }
                })
        } else if (this.wid) {
            // use wid to fetch the eid first
            this.workflowPersistService.retrieveWorkflowEnvironment(this.wid)
                .pipe(untilDestroyed(this))
                .subscribe({
                    next: env => {
                        this.environment.emit(env);
                        this.loadDatasetsOfEnvironment();
                    },
                    error: err => {
                        this.notificationService.error(`Runtime environment loading error!`)
                    }
                })
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
                                this.datasetsOfEnvironment.set([did, dvid], entry);
                            }
                        })
                    },
                    error: err => {
                        this.notificationService.error("Datasets of Environment loading error!");
                    }
                })
        }
    }

    onClickOpenLinkDatasetWindow() {
        const modalRef = this.modalService.open(NgbdModalEnvironmentDatasetAddComponent);
        modalRef.result.then((result) => {
            if (result) {
                console.log('Modal closed with:', result);
                // Handle the modal result if needed
            }
        }, (reason) => {
            // Handle the modal dismiss if needed
        });
    }
}
