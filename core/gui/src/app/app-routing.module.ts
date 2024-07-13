import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { environment } from "../environments/environment";
import { DashboardComponent } from "./dashboard/user/component/dashboard.component";
import { UserWorkflowComponent } from "./dashboard/user/component/user-workflow/user-workflow.component";
import { UserFileComponent } from "./dashboard/user/component/user-file/user-file.component";
import { UserQuotaComponent } from "./dashboard/user/component/user-quota/user-quota.component";
import { UserProjectSectionComponent } from "./dashboard/user/component/user-project/user-project-section/user-project-section.component";
import { UserProjectComponent } from "./dashboard/user/component/user-project/user-project.component";
import { WorkspaceComponent } from "./workspace/component/workspace.component";
import { HomeComponent } from "./home/component/home.component";
import { AuthGuardService } from "./common/service/user/auth-guard.service";
import { AdminUserComponent } from "./dashboard/admin/component/user/admin-user.component";
import { AdminExecutionComponent } from "./dashboard/admin/component/execution/admin-execution.component";
import { AdminGuardService } from "./dashboard/user/service/admin/guard/admin-guard.service";
import { SearchComponent } from "./dashboard/user/component/search/search.component";
import { FlarumComponent } from "./dashboard/user/component/flarum/flarum.component";
import { GmailComponent } from "./dashboard/admin/component/gmail/gmail.component";
import { UserDatasetExplorerComponent } from "./dashboard/user/component/user-dataset/user-dataset-explorer/user-dataset-explorer.component";
import { UserDatasetComponent } from "./dashboard/user/component/user-dataset/user-dataset.component";

const routes: Routes = [
  {
    path: "",
    component: WorkspaceComponent,
    canActivate: [AuthGuardService],
  },
  {
    path: "workflow/:id",
    component: WorkspaceComponent,
    canActivate: [AuthGuardService],
  },
];
if (environment.userSystemEnabled) {
  routes.push(
    {
      path: "dashboard",
      component: DashboardComponent,
      children: [
        {
          path: "home",
          component: HomeComponent,
        },
        {
          path: "user",
          canActivate: [AuthGuardService],
          children: [
            {
              path: "project",
              component: UserProjectComponent,
            },
            {
              path: "project/:pid",
              component: UserProjectSectionComponent,
            },
            {
              path: "workflow",
              component: UserWorkflowComponent,
            },
            {
              path: "file",
              component: UserFileComponent,
            },
            {
              path: "dataset",
              component: UserDatasetComponent,
            },
            // the below two URLs route to the same Component. The component will render the page accordingly
            {
              path: "dataset/:did",
              component: UserDatasetExplorerComponent,
            },
            {
              path: "dataset/create",
              component: UserDatasetExplorerComponent,
            },
            {
              path: "quota",
              component: UserQuotaComponent,
            },
            {
              path: "search",
              component: SearchComponent,
            },
            {
              path: "discussion",
              component: FlarumComponent,
            },
          ]
        },
        {
          path: "admin",
          canActivate: [AdminGuardService],
          children: [
            {
              path: "user",
              component: AdminUserComponent,
            },
            {
              path: "gmail",
              component: GmailComponent,
            },
            {
              path: "execution",
              component: AdminExecutionComponent,
            },
          ]
        },

      ],
    });
}
// redirect all other paths to index.
routes.push({
  path: "**",
  redirectTo: "",
});
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
