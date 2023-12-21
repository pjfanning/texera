import {Component, Input, OnInit} from '@angular/core';
import {DashboardEnvironment} from "../../../../dashboard/user/type/environment";
import {EnvironmentService} from "../../../../dashboard/user/service/user-environment/environment.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModalEnvironmentDatasetAddComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-environment-dataset-add/ngbd-modal-environment-dataset-add.component";
import {NgbdModalWorkflowEnvironmentSelectComponent} from "../../../../dashboard/user/component/user-environment/ngbd-modal-workflow-environment-select/ngbd-modal-workflow-environment-select.component";
import {WorkflowEnvironmentService} from "../../../../common/service/workflow-environment/workflow-environment.service";

@Component({
  selector: 'texera-environment-property-edit-frame',
  templateUrl: './environment-property-edit-frame.component.html',
  styleUrls: ['./environment-property-edit-frame.component.scss']
})
export class EnvironmentPropertyEditFrameComponent implements OnInit {
  @Input()
  eid: number = 0;

  @Input()
  wid: number = 0;

  @Input()
  isEnvSwitchable = true;

  environment: DashboardEnvironment | undefined;

  environmentName: string = "";
  environmentDatasets: any[] = [{name: 'Dataset 1', files: ['file1.csv', 'file2.csv']},
    {name: 'Dataset 2', files: ['file3.csv', 'file4.csv']},
    {name: 'Dataset 3', files: ['file5.csv', 'file6.csv']},
    {name: 'Dataset 4', files: ['file7.csv', 'file8.csv']},
    {name: 'Dataset 5', files: ['file9.csv', 'file10.csv']}];

  constructor(
    private environmentService: EnvironmentService,
    private workflowEnvironmentService: WorkflowEnvironmentService,
    private modalService: NgbModal,
  ) {
  }

  onClickSwitchEnvironment(): void {
    const modalRef = this.modalService.open(NgbdModalWorkflowEnvironmentSelectComponent, {
      backdrop: 'static',  // ensures the background is not clickable
      keyboard: false      // ensures the modal cannot be closed with the keyboard
    });

    modalRef.componentInstance.isEditingWorkflow = true;

    modalRef.result.then(
      (selectedEnvironmentID: number | null) => {
        if (selectedEnvironmentID == null) {
          // If an environment was not selected, create a new one and relate it
          // Here, you can perform further actions with the selected environment
          this.environmentService.addEnvironment(
            {
              eid: undefined,
              uid: undefined,
              name: "Untitled Environment",
              description: "Some description",
              creationTime: undefined
            }
          ).subscribe(env => {
            const wid = this.wid;
            const eid = env.environment.eid;
            if (wid && eid) {
              this.workflowEnvironmentService.bindWorkflowWithEnvironment(wid, eid).subscribe(res => {
                console.log(`bind with new env, wid: ${wid}, eid: ${env.environment.eid}`)
                this.eid = eid;
                this.initEditor();
              });
            }
          })
        } else {
          // user choose a existing environment
          const wid = this.wid;
          if (wid) {

            this.workflowEnvironmentService.bindWorkflowWithEnvironment(wid, selectedEnvironmentID).subscribe(res => {
              console.log(`bind with new env, wid: ${wid}, eid: ${selectedEnvironmentID}`)
              this.eid = selectedEnvironmentID;
              this.initEditor();
            });
          }
        }
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
          // TODO: placeholder for datasets
          // console.log('Returned value from modal:', dsName);  // Will log 'someReturnValue'
          // this.environment.environment.inputs.push(dsName);
          // this.environmentService.updateEnvironment(this.environment.environment.eid, this.environment)
        }
      },
      (reason) => {
        console.log('Dismissed with:', reason);  // This will run if modal was dismissed without a return value.
      }
    );

  }

  ngOnInit(): void {
    console.log(this.eid)
    this.initEditor();
  }

  initEditor(): void {
    if (this.eid) {
      this.environmentService.retrieveEnvironmentByEid(this.eid).subscribe(env => {
        if (env == null) {
          throw new Error("Environment not exists!!!");
        } else {
          console.log("retrieve the environment: ", env)
          this.environment = env;
          this.initEnvironmentDisplay();
           console.log(this.environmentDatasets)
        }
      });
    } else {
      this.environmentName = "Runtime environment is not set"
    }
  }

  initEnvironmentDisplay(): void {
    if (this.environment) {
      this.environmentName = this.environment.environment.name;

      // if (this.environment.datasets)
      //   this.environmentDatasets = this.environment.datasets;
      // // dummy for development use
      // this.environmentDatasets = [{name: 'Dataset 1', files: ['file1.csv', 'file2.csv']},
      //   {name: 'Dataset 2', files: ['file3.csv', 'file4.csv']},
      //   {name: 'Dataset 3', files: ['file5.csv', 'file6.csv']},
      //   {name: 'Dataset 4', files: ['file7.csv', 'file8.csv']},
      //   {name: 'Dataset 5', files: ['file9.csv', 'file10.csv']}];
    }
  }

  close(): void {

  }
}
