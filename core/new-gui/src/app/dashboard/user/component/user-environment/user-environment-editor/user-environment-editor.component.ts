import { ActivatedRoute, Router } from "@angular/router";
import { UntilDestroy } from "@ngneat/until-destroy";
import { Component, OnInit } from "@angular/core";
import { DashboardEnvironment, DatasetOfEnvironmentDetails } from "../../../type/environment";
import { EnvironmentService } from "../../../service/user-environment/environment.service";
import { NotificationService } from "../../../../../common/service/notification/notification.service";
import { DashboardDataset } from "../../../type/dashboard-dataset.interface";

@UntilDestroy()
@Component({
  selector: "texera-user-environment-editor",
  templateUrl: "user-environment-editor.component.html",
  styleUrls: ["user-environment-editor.component.scss"],
})
export class UserEnvironmentEditorComponent implements OnInit {
  environment: DashboardEnvironment | undefined;
  eid: number = 0;
  selectedMenu: "datasets" = "datasets";

  datasetsOfEnvironment: Map<[number, number], DatasetOfEnvironmentDetails> = new Map();
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private environmentService: EnvironmentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      // Get the eid from the params
      this.eid = +params["eid"]; // The '+' converts the string to a number

      this.environmentService.retrieveEnvironmentByEid(this.eid).subscribe({
        next: env => {
          if (env) {
            this.environment = env;
            // load the datasets
            this.loadDatasetsOfEnvironment();
          }
        },
        error: (err: unknown) => {
          this.notificationService.error("Environment loading encounters error");
        },
      });
    });
  }

  private loadDatasetsOfEnvironment() {
    if (this.environment && this.environment.environment.eid) {
      const eid = this.environment.environment.eid;
      this.environmentService.retrieveDatasetsOfEnvironmentDetails(eid).subscribe({
        next: datasets => {
          datasets.forEach(entry => {
            const did = entry.dataset.did;
            const dvid = entry.version.dvid;
            if (did && dvid) {
              this.datasetsOfEnvironment.set([did, dvid], entry);
            }
          });
        },
        error: (err: unknown) => {
          this.notificationService.error("Datasets of Environment loading error!");
        },
      });
    }
  }
  get environmentName(): string {
    if (this.environment) {
      return this.environment.environment.name;
    }
    return "";
  }

  get environmentCreationTime(): number {
    if (this.environment) {
      if (this.environment.environment.creationTime) {
        return this.environment.environment.creationTime;
      }
    }
    return 0;
  }

  get environmentDescription(): string {
    if (this.environment) {
      return this.environment.environment.description;
    }

    return "";
  }
}
