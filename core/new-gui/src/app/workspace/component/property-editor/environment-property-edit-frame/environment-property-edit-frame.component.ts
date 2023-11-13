import {Component, Input, OnInit} from '@angular/core';
import {DashboardEnvironment} from "../../../../dashboard/user/type/environment";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModalEnvironmentDatasetAddComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-environment-dataset-add/ngbd-modal-environment-dataset-add.component";
import {NgbdModalWorkflowEnvironmentSelectComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-workflow-environment-select/ngbd-modal-workflow-environment-select.component";

@Component({
  selector: 'texera-environment-property-edit-frame',
  templateUrl: './environment-property-edit-frame.component.html',
  styleUrls: ['./environment-property-edit-frame.component.scss']
})
export class EnvironmentPropertyEditFrameComponent implements OnInit{
  @Input()
  eid: number = 0;

  @Input()
  wid : number = 0;

  @Input()
  isEnvSwitchable = true;

  environment: DashboardEnvironment | undefined;

  constructor(
    private environmentService: EnvironmentService,
    private modalService: NgbModal,
  ) {}

  onClickSwitchEnvironment(): void {
    const modalRef = this.modalService.open(NgbdModalWorkflowEnvironmentSelectComponent, {
      backdrop: 'static',  // ensures the background is not clickable
      keyboard: false      // ensures the modal cannot be closed with the keyboard
    });

    modalRef.result.then(
      (selectedEnvironmentID: number | null) => {
        if (selectedEnvironmentID == null) {
          // If an environment was not selected, create a new one and relate it
          // Here, you can perform further actions with the selected environment
          const eid = this.environmentService.addEnvironment(
            {
              environment: {
                eid: 0,
                name: "Untitled Environment",
                description: "Some description",
                creationTime: Date.now(),
                inputs: [],
                outputs: [],
              },
              ownerName: "Hola"
            }
          )
          const wid = this.wid;
          if (wid)
            this.environmentService.switchEnvironmentOfWorkflow(wid, eid);
            this.eid = eid;
        } else {
          // user choose a existing environment
          const wid = this.wid;
          if (wid)
            this.environmentService.switchEnvironmentOfWorkflow(wid, selectedEnvironmentID);
            this.eid = selectedEnvironmentID;
        }
        this.initEditor();
      },
      (reason) => {
        console.log('Modal was dismissed.', reason);
      }
    );
  }

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
    this.initEditor();
  }

  initEditor(): void {
    const env = this.environmentService.retrieveEnvironmentByEid(this.eid);
    if (env == null) {
      throw new Error("Environment not exists!!!");
    } else {
      this.environment = env;
    }
  }

  close(): void {

  }
}
