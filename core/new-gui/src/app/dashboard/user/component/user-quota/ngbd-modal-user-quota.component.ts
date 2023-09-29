import { AfterViewInit, Component, Input, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { AdminUserService } from "../../../admin/service/admin-user.service";
import { File, Workflow, mongoExecution, mongoWorkflow } from "../../../../common/type/user"

import * as c3 from "c3";
import { ChartType } from "src/app/workspace/types/visualization.interface";


@UntilDestroy()
@Component({
  templateUrl: './ngbd-modal-user-quota.component.html',
  styleUrls: ['./ngbd-modal-user-quota.component.scss']
})

export class NgbModalUserQuotaComponent implements OnInit {
  public static readonly MONGODB_PIE_CHART = "#mongodb-storage-pie-chart";
  public static readonly MONGODB_BAR_CHART = "#uploaded-file-bar-chart";

  public static readonly WIDTH = 300;
  public static readonly HEIGHT = 300;
  public static readonly BARCHARTSIZE = 600;

  userUid: number = 0;
  totalSize: string = "";
  createdFiles: ReadonlyArray<File> = [];
  createdWorkflows: ReadonlyArray<Workflow> = [];
  accessFiles: ReadonlyArray<number> = [];
  accessWorkflows: ReadonlyArray<number> = [];
  topFiveFiles: ReadonlyArray<File> = [];
  mongodbExecutions: ReadonlyArray<mongoExecution> = [];
  mongodbWorkflows: Array<mongoWorkflow> = [];

  constructor(private adminUserService: AdminUserService, public activeModel: NgbActiveModal) {
  }

  ngOnInit(): void {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        let size = 0;
        this.createdFiles.forEach(file => size += file.fileSize / 1000000)
        this.totalSize = size.toPrecision(2);

        const copiedFiles = [...fileList];
        copiedFiles.sort((a, b) => b.fileSize - a.fileSize);
        this.topFiveFiles = copiedFiles.slice(0, 5);
      });
    
    this.adminUserService
      .getCreatedWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(workflowList => {
        this.createdWorkflows = workflowList;
      });
    
    this.adminUserService
      .getAccessFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessFiles => {
        this.accessFiles = accessFiles;
      });

    this.adminUserService
      .getAccessWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessWorkflows => {
        this.accessWorkflows = accessWorkflows;
      });

    this.adminUserService
      .getMongoDBs(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(mongoList => {
        this.mongodbExecutions = mongoList;
        this.mongodbWorkflows = [];

        this.mongodbExecutions.forEach(execution => {
          let insert = false;

          this.mongodbWorkflows.some((workflow, index, array) => {
            if (workflow.workflowName === execution.workflowName) {
                array[index].executions.push(execution);
                insert = true;
                return
            }
          });

          if (!insert) {
            let workflow: mongoWorkflow = {
              workflowName: execution.workflowName,
              executions: [] as mongoExecution[]
            };
            workflow.executions.push(execution);
            this.mongodbWorkflows.push(workflow);
          }

        })
      });

  }

  deleteMongoCollection(collectionName: string, execution: mongoExecution, workflowName: string) {
    this.adminUserService
      .deleteMongoDBCollection(collectionName)
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        // this.ngOnInit();
        this.mongodbWorkflows.some((workflow, index, array) => {
          if (workflow.workflowName === workflowName) {
              array[index].executions = array[index].executions.filter(e => e !== execution);
          }
        });
      });
  }

  /**
   * Convert a numeric timestamp to a human-readable time string.
   */
  convertTimeToTimestamp(timeValue: number): string {
    const date = new Date(timeValue);
    return date.toLocaleString("en-US", { timeZoneName: "short" });
  }

}