import { AfterViewInit, Component, Input, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { AdminUserService } from "../../../admin/service/admin-user.service";
import { File, Workflow } from "../../../../common/type/user"


@UntilDestroy()
@Component({
  templateUrl: './ngbd-modal-user-quota.component.html',
  styleUrls: ['./ngbd-modal-user-quota.component.scss']
})

export class NgbModalUserQuotaComponent implements OnInit{
  userUid: number = 0;
  totalSize: string = "";
  createdFiles: ReadonlyArray<File> = [];
  createdWorkflows: ReadonlyArray<Workflow> = [];
  accessFiles: ReadonlyArray<number> = [];
  accessWorkflows: ReadonlyArray<number> = [];
  topfiveFilesizes: ReadonlyArray<string> = [];

  constructor(private adminUserService: AdminUserService) {
  }

  ngOnInit(): void {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        let size = 0;
        this.createdFiles.forEach(file => size += file.file_size / 1000000)
        this.totalSize = size.toPrecision(2);
        console.log("number of files uploaded");
        console.log(this.createdFiles.length);

        const copiedFiles = [...this.createdFiles];
        copiedFiles.sort((a, b) => b.file_size - a.file_size);
        const topFiveFiles = copiedFiles.slice(0, 5);
        const fileSizes = topFiveFiles.map(file => (file.file_size / 1000000).toPrecision(2));
        this.topfiveFilesizes = fileSizes;
      });
    
    this.adminUserService
      .getCreatedWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(workflowList => {
        this.createdWorkflows = workflowList;
        // console.log("number of workflows created");
        // console.log(this.createdWorkflows.length);
      });
    
    this.adminUserService
      .getAccessFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessFiles => {
        this.accessFiles = accessFiles;
        // console.log("number of files that can be accessed");
        // console.log(this.accessFiles.length);
      });

    this.adminUserService
      .getAccessWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessWorkflows => {
        this.accessWorkflows = accessWorkflows;
        // console.log("number of workflows that can be accessed");
        // console.log(this.accessWorkflows.length);
      });
    
    this.adminUserService
      .getMongoDBSize()
      .pipe(untilDestroyed(this))
      .subscribe(size => {
        console.log("the size of mongodb: ");
        console.log(size);
        console.log("=======");
      });
    }
}