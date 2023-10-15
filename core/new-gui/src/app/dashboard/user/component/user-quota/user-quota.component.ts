import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { AdminUserService } from "../../../admin/service/admin-user.service";
import { File, Workflow, mongoExecution, mongoWorkflow } from "../../../../common/type/user"
import { UserFileService } from "../../service/user-file/user-file.service";
import { NzTableSortFn } from "ng-zorro-antd/table";
import { MatDialog } from '@angular/material/dialog';
import { UserService } from "../../../../common/service/user/user.service";



@UntilDestroy()
@Component({
  templateUrl: './user-quota.component.html',
  styleUrls: ['./user-quota.component.scss']
})

export class UserQuotaComponent implements OnInit {
  userUid: number = 0;
  totalFileSize: number = 0;
  totalMongoSize: number = 0;
  createdFiles: ReadonlyArray<File> = [];
  createdWorkflows: ReadonlyArray<Workflow> = [];
  accessFiles: ReadonlyArray<number> = [];
  accessWorkflows: ReadonlyArray<number> = [];
  topFiveFiles: ReadonlyArray<File> = [];
  mongodbExecutions: ReadonlyArray<mongoExecution> = [];
  mongodbWorkflows: Array<mongoWorkflow> = [];

  constructor(private adminUserService: AdminUserService, private userService: UserService, public activeModel: NgbActiveModal, private userFileService: UserFileService, private dialog: MatDialog) 
  {
    // this.userUid = this.userService.getCurrentUser()?.uid;
    // this.userUid = this.userService.getCurrentUser()!.uid;
    // this does not check if the currentUser(this.userService.getCurrentUser())
    // is undefined or not.
    this.userUid = 1;
  }

  ngOnInit(): void {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        let size = 0;
        this.createdFiles.forEach(file => {size += file.fileSize})
        this.totalFileSize = size;

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
        this.totalMongoSize = 0;
        this.mongodbExecutions = mongoList;
        this.mongodbWorkflows = [];

        this.mongodbExecutions.forEach(execution => {
          let insert = false;
          this.totalMongoSize += execution.size;

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
        this.mongodbWorkflows.some((workflow, index, array) => {
          if (workflow.workflowName === workflowName) {
              array[index].executions = array[index].executions.filter(e => e !== execution);
              this.totalMongoSize -= execution.size;
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

  deleteFile(fid: number) {
    if (fid === undefined) {
      return;
    }
    this.userFileService
      .deleteFile(fid)
      .pipe(untilDestroyed(this))
      .subscribe(() =>
        this.refreshFiles()
      );
  }

  downloadFile(fid: number, fileName: string) {
    this.userFileService
      .downloadFile(fid)
      .pipe(untilDestroyed(this))
      .subscribe((response: Blob) => {
        const link = document.createElement("a");
        link.download = fileName;
        link.href = URL.createObjectURL(new Blob([response]));
        link.click();
      });
  }

  convertFileSize(sizeInBytes: number): string {
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];

    let size = sizeInBytes;
    let unitIndex = 0;

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }

    return `${size.toFixed(2)} ${units[unitIndex]}`;
  }

  refreshFiles() {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        let size = 0;
        this.createdFiles.forEach(file => {size += file.fileSize})
        this.totalFileSize = size;

        const copiedFiles = [...fileList];
        copiedFiles.sort((a, b) => b.fileSize - a.fileSize);
        this.topFiveFiles = copiedFiles.slice(0, 5);
      });
  }

  maxStringLength(input: string, length: number): string {
    if (input.length > length) {
      return input.substring(0, length) + " . . . ";
    }
    return input;
  }

  public sortByMongoDBSize: NzTableSortFn<mongoExecution> = (a: mongoExecution, b: mongoExecution) =>
    b.size - a.size;

}
// for debug use

// import {AfterViewInit, Component, OnInit} from "@angular/core";
// import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
// export class WorkFlow {
//   public title: string = "";
//   public users: string = "";
//   public state: string = "";
//   public time: string = "";
//   public constructor(title: string, users: string, state: string, time: string){
//     this.title = title;
//     this.users = users;
//     this.state = state;
//     this.time = time;
//   }
// }
// @UntilDestroy()
// @Component({
//   templateUrl: "./user-quota.component.html",
//   styleUrls: ["./user-quota.component.scss"],
// })
// export class UserQuotaComponent implements OnInit, AfterViewInit{

//   workflows: Array<WorkFlow> = [];
//   workflowsCount: number = 1;
//   usersCount: number = 0;
//   workflows_info = [...this.workflows];


//   constructor() {
//   }

//   ngOnInit() {
//     const test = new WorkFlow("workflow1", "ZheYuan", "active", "1:36")
//     this.workflows.push(test);
//     this.workflows_info = [...this.workflows];
//   }

//   ngAfterViewInit() {
//   }

// }
