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
}
