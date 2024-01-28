import {ActivatedRoute, Router} from '@angular/router';
import {UntilDestroy} from "@ngneat/until-destroy";
import {Component, OnInit} from "@angular/core";
import {DashboardEnvironment} from "../../../type/environment";
import {EnvironmentService} from "../../../service/user-environment/environment.service";

@UntilDestroy()
@Component({
  selector: "texera-user-environment-editor",
  templateUrl: "user-environment-editor.component.html",
  styleUrls: ["user-environment-editor.component.scss"]
})
export class UserEnvironmentEditorComponent implements OnInit {
  environment: DashboardEnvironment | undefined;
  eid : number = 0;
  selectedMenu: "datasets" = "datasets";


  constructor(
    private router: Router,
    private activatedRoute : ActivatedRoute,
    private environmentService: EnvironmentService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      // Get the eid from the params
      this.eid = +params['eid'];  // The '+' converts the string to a number

      this.environmentService.retrieveEnvironmentByEid(this.eid).subscribe(env => {
        if (env) {
          this.environment = env;
        }
      })
    });
  }

  get environmentName(): string {
    if (this.environment) {
      return this.environment.environment.name;
    }
    return ''
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

    return '';
  }
}
