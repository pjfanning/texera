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
  userUid: number = 0
  createdFiles: ReadonlyArray<File> = [];
  createdWorkflows: ReadonlyArray<Workflow> = [];
  accessFiles: ReadonlyArray<number> = [];
  accessWorkflows: ReadonlyArray<number> = [];

  constructor(private adminUserService: AdminUserService) {
  }

  ngOnInit(): void {
    this.adminUserService
      .getUploadedFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(fileList => {
        this.createdFiles = fileList;
        console.log("number of files uploaded");
        console.log(this.createdFiles.length);
      });
    
    this.adminUserService
      .getCreatedWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(workflowList => {
        this.createdWorkflows = workflowList;
        console.log("number of workflows created");
        console.log(this.createdWorkflows.length);
      });
    
    this.adminUserService
      .getAccessFiles(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessFiles => {
        this.accessFiles = accessFiles;
        console.log("number of files that can be accessed");
        console.log(this.accessFiles.length);
      });

    this.adminUserService
      .getAccessWorkflows(this.userUid)
      .pipe(untilDestroyed(this))
      .subscribe(accessWorkflows => {
        this.accessWorkflows = accessWorkflows;
        console.log("number of workflows that can be accessed");
        console.log(this.accessWorkflows.length);
      });
  }
}