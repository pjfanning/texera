import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { ActivatedRoute, Router } from "@angular/router";
import { Location } from "@angular/common";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { User } from "src/app/common/type/user";
import { UserService } from "src/app/common/service/user/user.service";
import { NzModalService } from "ng-zorro-antd/modal";
import { LocalLoginComponent } from "../../home/local-login/local-login.component";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-result",
  templateUrl: "hub-workflow-detail.component.html",
  styleUrls: ["hub-workflow-detail.component.scss"],
})
export class HubWorkflowDetailComponent implements OnInit {
  wid: string | null;
  clonedWorklowId: number | undefined;
  workflowName: string | null;
  ownerUser!: User;
  private currentUser?: User;
  public clonePrompt: String = "";
  workflow = {
    steps: [
      {
        name: "Step 1: Data Collection",
        description: "Collect necessary data from various sources.",
        status: "Completed",
      },
      {
        name: "Step 2: Data Analysis",
        description: "Analyze the collected data for insights.",
        status: "In Progress",
      },
      {
        name: "Step 3: Report Generation",
        description: "Generate reports based on the analysis.",
        status: "Not Started",
      },
      {
        name: "Step 4: Presentation",
        description: "Present the findings to stakeholders.",
        status: "Not Started",
      },
    ],
  };

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private hubWorkflowService: HubWorkflowService,
    private router: Router,
    private userService: UserService,
    private modalService: NzModalService,
  ) {
    this.wid = this.route.snapshot.queryParamMap.get("wid");
    this.workflowName = this.route.snapshot.queryParamMap.get("name");
  }

  ngOnInit() {
    this.hubWorkflowService
      .getOwnerUser(Number(this.wid))
      .pipe(untilDestroyed(this))
      .subscribe({
        next: owner => {
          this.ownerUser = owner;
          console.log(this.ownerUser.uid);
          this.currentUser = this.userService.getCurrentUser();
          if (!this.currentUser || this.currentUser.uid !== this.ownerUser.uid) {
            this.clonePrompt = "Copy & Edit";
          } else {
            this.clonePrompt = "Edit";
          }
        },
      });
  }

  goBack(): void {
    this.location.back();
  }

  cloneWorkflow(): void {
    if (!this.currentUser) {
      this.modalService.create({
        nzContent: LocalLoginComponent,
        nzTitle: "Login",
        nzFooter: null,
        nzCentered: true,
      });
    } else if (this.currentUser.uid !== this.ownerUser.uid) {
      this.hubWorkflowService.cloneWorkflow(Number(this.wid))
        .pipe(untilDestroyed(this))
        .subscribe(newWid => {
          this.clonedWorklowId = newWid;
          this.router.navigate([`/workflow/${this.clonedWorklowId}`]);
        });
    } else {
      this.router.navigate([`/workflow/${this.wid}`]);
    }
  }
}
