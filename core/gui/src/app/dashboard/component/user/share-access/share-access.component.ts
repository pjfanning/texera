import { Component, inject, Input, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ShareAccessService } from "../../../service/user/share-access/share-access.service";
import { ShareAccess } from "../../../type/share-access.interface";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { UserService } from "../../../../common/service/user/user.service";
import { GmailService } from "../../../../common/service/gmail/gmail.service";
import { NZ_MODAL_DATA, NzModalRef, NzModalService } from "ng-zorro-antd/modal";
import { NotificationService } from "../../../../common/service/notification/notification.service";
import { HttpErrorResponse } from "@angular/common/http";
import { WorkflowPersistService } from "src/app/common/service/workflow-persist/workflow-persist.service";
import { WorkflowActionService } from "src/app/workspace/service/workflow-graph/model/workflow-action.service";

@UntilDestroy()
@Component({
  templateUrl: "share-access.component.html",
  styleUrls: ["share-access.component.scss"]
})
export class ShareAccessComponent implements OnInit {
  readonly nzModalData = inject(NZ_MODAL_DATA);
  readonly writeAccess: boolean = this.nzModalData.writeAccess;
  readonly type: string = this.nzModalData.type;
  readonly id: number = this.nzModalData.id;
  readonly allOwners: string[] = this.nzModalData.allOwners;
  readonly inWorkspace: boolean = this.nzModalData.inWorkspace;
  public validateForm: FormGroup;
  public accessList: ReadonlyArray<ShareAccess> = [];
  public owner: string = "";
  public filteredOwners: Array<string> = [];
  public ownerSearchValue?: string;
  currentEmail: string | undefined = "";
  isPublic: boolean | null = null;
  constructor(
    private accessService: ShareAccessService,
    private formBuilder: FormBuilder,
    private userService: UserService,
    private gmailService: GmailService,
    private notificationService: NotificationService,
    private modalService: NzModalService,
    private workflowPersistService: WorkflowPersistService,
    private workflowActionService: WorkflowActionService,
  ) {
    this.validateForm = this.formBuilder.group({
      email: [null, [Validators.email, Validators.required]],
      accessLevel: ["READ"],
    });
    this.currentEmail = this.userService.getCurrentUser()?.email;
    
  }

  ngOnInit(): void {
    this.accessService
      .getAccessList(this.type, this.id)
      .pipe(untilDestroyed(this))
      .subscribe(access => (this.accessList = access));
    this.accessService
      .getOwner(this.type, this.id)
      .pipe(untilDestroyed(this))
      .subscribe(name => {
        this.owner = name;
      });
    this.workflowPersistService
      .getWorkflowIsPublished(this.id)
      .pipe(untilDestroyed(this))
      .subscribe(type => {
        this.isPublic = type === "Public";
      });
  }

  public onChange(value: string): void {
    if (value === undefined) {
      this.filteredOwners = [];
    } else {
      this.filteredOwners = this.allOwners.filter(owner => owner.toLowerCase().indexOf(value.toLowerCase()) !== -1);
    }
  }

  public grantAccess(): void {
    if (this.validateForm.valid) {
      this.accessService
        .grantAccess(this.type, this.id, this.validateForm.value.email, this.validateForm.value.accessLevel)
        .pipe(untilDestroyed(this))
        .subscribe({
          next: () => {
            this.ngOnInit();
            this.notificationService.success(
              this.type + " shared with " + this.validateForm.value.email + " successfully."
            );
            this.gmailService.sendEmail(
              "Texera: " + this.owner + " shared a " + this.type + " with you",
              this.owner +
              " shared a " +
              this.type +
              " with you, access the workflow at " +
              location.origin +
              "/workflow/" +
              this.id,
              this.validateForm.value.email
            );
          },
          error: (error: unknown) => {
            if (error instanceof HttpErrorResponse) {
              this.notificationService.error(error.error.message);
            }
          },
        });
    }
  }

  public revokeAccess(userToRemove: string): void {
    this.accessService
      .revokeAccess(this.type, this.id, userToRemove)
      .pipe(untilDestroyed(this))
      .subscribe(() => this.ngOnInit());
  }

  verifyPublish(): void {
    if (!this.isPublic) {
      const modal: NzModalRef = this.modalService.create({
        nzTitle: 'Notice',
        nzContent: 'Publishing your workflow would grant all Texera users read access to your workflow along with the right to clone your work.',
        nzFooter: [
          {
            label: 'Cancel',
            onClick: () => modal.close()
          },
          {
            label: 'Publish',
            type: 'primary',
            onClick: () => {
              this.publishWorkflow()
              if (this.inWorkspace) {
                this.workflowActionService.setWorkflowIsPublished(1)
              }
              modal.close()
            }
          }
        ]
      });
    }
    
  }
  verifyUnpublish(): void {
    if (this.isPublic) {
      const modal: NzModalRef = this.modalService.create({
        nzTitle: 'Notice',
        nzContent: 'All other users would lose access to your work if you unpublish it.',
        nzFooter: [
          {
            label: 'Cancel',
            onClick: () => modal.close()
          },
          {
            label: 'Unpublish',
            type: 'primary',
            onClick: () => {
              this.unpublishWorkflow()
              if (this.inWorkspace) {
                this.workflowActionService.setWorkflowIsPublished(0)
              }
              modal.close()
            }
          }
        ]
      });
    }
  }

  public publishWorkflow(): void {
    if (!this.isPublic) {
      console.log("Workflow " + this.id + " is published")
      this.workflowPersistService
        .updateWorkflowIsPublished(this.id, true)
        .pipe(untilDestroyed(this))
        .subscribe(() => this.isPublic = true);
    }
    else {
      console.log("Workflow " + this.id + " is already published")
    }
  }
  public unpublishWorkflow(): void {
    if (this.isPublic) {
      console.log("Workflow " + this.id + " is unpublished")
      this.workflowPersistService
        .updateWorkflowIsPublished(this.id, false)
        .pipe(untilDestroyed(this))
        .subscribe(() => this.isPublic = false);
    }
    else {
      console.log("Workflow " + this.id + " is already private")
    }
  }
}
