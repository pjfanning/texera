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
import { HomeComponent } from "./hub/component/home/home.component";
import { AuthGuardService } from "./common/service/user/auth-guard.service";
import { AdminUserComponent } from "./dashboard/user/component/admin/user/admin-user.component";
import { AdminExecutionComponent } from "./dashboard/user/component/admin/execution/admin-execution.component";
import { AdminGuardService } from "./dashboard/user/service/admin/guard/admin-guard.service";
import { SearchComponent } from "./dashboard/user/component/search/search.component";
import { FlarumComponent } from "./dashboard/user/component/flarum/flarum.component";
import { AdminGmailComponent } from "./dashboard/user/component/admin/gmail/admin-gmail.component";
import { UserDatasetExplorerComponent } from "./dashboard/user/component/user-dataset/user-dataset-explorer/user-dataset-explorer.component";
import { UserDatasetComponent } from "./dashboard/user/component/user-dataset/user-dataset.component";
import { HubWorkflowSearchComponent } from "./hub/component/workflow/search/hub-workflow-search.component";
import { HubWorkflowResultComponent } from "./hub/component/workflow/result/hub-workflow-result.component";
import { HubWorkflowComponent } from "./hub/component/workflow/hub-workflow.component";
import { HubWorkflowDetailComponent } from "./hub/component/workflow/detail/hub-workflow-detail.component";

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
  routes.push({
    path: "dashboard",
    component: DashboardComponent,
    children: [
      {
        path: "home",
        component: HomeComponent,
      },
      {
        path: "hub",
        children: [
          {
            path: "workflow",
            component: HubWorkflowComponent,
            children: [
              {
                path: "search",
                component: HubWorkflowSearchComponent,
              },
              {
                path: "search/result",
                component: HubWorkflowResultComponent,
              },
              {
                path: "search/result/detail",
                component: HubWorkflowDetailComponent,
              },
            ],
          },
        ],
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
        ],
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
            component: AdminGmailComponent,
          },
          {
            path: "execution",
            component: AdminExecutionComponent,
          },
        ],
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
