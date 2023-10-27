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

  constructor(
    private router: Router,
    private activatedRoute : ActivatedRoute,
    private environmentService: EnvironmentService) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      // Get the eid from the params
      this.eid = +params['eid'];  // The '+' converts the string to a number

      const env = this.environmentService.getEnvironmentByIndex(this.eid);
      if (env == null) {
        this.environment = undefined;
      } else {
        this.environment = env;
      }
    });
  }
}
