import {Component, Input, OnInit} from '@angular/core';
import {DashboardEnvironment} from "../../../../dashboard/user/type/environment";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";

@Component({
  selector: 'texera-environment-property-edit-frame',
  templateUrl: './environment-property-edit-frame.component.html',
  styleUrls: ['./environment-property-edit-frame.component.scss']
})
export class EnvironmentPropertyEditFrameComponent implements OnInit{
  @Input()
  eid: number = 0;

  environment: DashboardEnvironment | undefined;

  constructor(
    private environmentService: EnvironmentService,
  ) {}

  onClickOpenAddDatasetWindow(): void {

  }

  ngOnInit(): void {
    const env = this.environmentService.getEnvironmentByIndex(this.eid);
    if (env == null) {
      throw new Error("Environment not exists!!!");
    } else {
      this.environment = env;
    }
  }

  close(): void {

  }
}
