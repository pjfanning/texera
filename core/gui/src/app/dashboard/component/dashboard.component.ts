import { Component, OnInit } from "@angular/core";
import { WorkflowPersistService } from "../../common/service/workflow-persist/workflow-persist.service";
import { UserService } from "../../common/service/user/user.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { FlarumService } from "../service/user/flarum/flarum.service";
import { HttpErrorResponse } from "@angular/common/http";
import { Router } from "@angular/router";

/**
 * dashboardComponent is the component which contains all the subcomponents
 * on the user dashboard. The subcomponents include Top bar, feature bar,
 * and feature container.
 *
 * @author Zhaomin Li
 */
@Component({
  selector: "texera-dashboard",
  templateUrl: "./dashboard.component.html",
  styleUrls: ["./dashboard.component.scss"],
  providers: [WorkflowPersistService],
})
@UntilDestroy()
export class DashboardComponent implements OnInit {
  isAdmin = this.userService.isAdmin();
  displayForum = true;
  displayNavbar = true;
  routesWithoutNavbar = ["/workspace"];
  constructor(
    private userService: UserService,
    private flarumService: FlarumService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!document.cookie.includes("flarum_remember")) {
      this.flarumService
        .auth()
        .pipe(untilDestroyed(this))
        .subscribe({
          next: (response: any) => {
            document.cookie = `flarum_remember=${response.token};path=/`;
          },
          error: (err: unknown) => {
            if ((err as HttpErrorResponse).status == 404) {
              this.displayForum = false;
            } else {
              this.flarumService
                .register()
                .pipe(untilDestroyed(this))
                .subscribe(() => this.ngOnInit());
            }
          },
        });
    }
    this.router.events.subscribe(() => {
      this.checkRoute();
    });
  }

  checkRoute() {
    const currentRoute = this.router.url;
    this.displayNavbar = this.isNavbarEnabled(currentRoute);
    this.handleCollapseChange();
  }

  isNavbarEnabled(currentRoute: string) {
    for (const routeWithoutNavbar of this.routesWithoutNavbar) {
      if (currentRoute.includes(routeWithoutNavbar)) {
        return false;
      }
    }
    return true;
  }

  handleCollapseChange() {
    const resizeEvent = new Event("resize");
    const editor = document.getElementById("workflow-editor");
    setTimeout(() => {
      if (editor) {
        window.dispatchEvent(resizeEvent);
      }
    }, 175);
  }
}
