import {Component, Input, OnInit} from '@angular/core';
import {DashboardEnvironment} from "../../../../dashboard/user/type/environment";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModalEnvironmentDatasetAddComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-environment-dataset-add/ngbd-modal-environment-dataset-add.component";

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
    private modalService: NgbModal,
  ) {}

  onClickOpenAddDatasetWindow(): void {
    const modalRef = this.modalService.open(NgbdModalEnvironmentDatasetAddComponent)
    // Capture the returned value.
    modalRef.result.then(
      (dsName) => {
        if (this.environment) {
          console.log('Returned value from modal:', dsName);  // Will log 'someReturnValue'
          this.environment.environment.inputs.push(dsName);
          this.environmentService.updateEnvironment(this.environment.environment.eid, this.environment)
        }
      },
      (reason) => {
        console.log('Dismissed with:', reason);  // This will run if modal was dismissed without a return value.
      }
    );

  }

  ngOnInit(): void {
    console.log(this.eid)
    console.log(this.environmentService.getAllEnvironments())
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
