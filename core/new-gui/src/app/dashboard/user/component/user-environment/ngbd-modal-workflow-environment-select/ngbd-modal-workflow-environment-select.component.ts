import { UntilDestroy } from "@ngneat/until-destroy";
import { Component, OnInit } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { EnvironmentService } from "../../../service/user-environment/environment.service";
import {DashboardEnvironment} from "../../../type/environment";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-workflow-environment-select.component',
  templateUrl: './ngbd-modal-workflow-environment-select.component.html',
  styleUrls: ['./ngbd-modal-workflow-environment-select.component.scss']
})
export class NgbdModalWorkflowEnvironmentSelectComponent implements OnInit {
  selectedEnvironmentId: number | null = null;
  environments: DashboardEnvironment[] = [];

  constructor(
    private activeModal: NgbActiveModal,
    private environmentService: EnvironmentService) {}

  ngOnInit(): void {
    const environmentIdentifiers = this.environmentService.retrieveEnvironments();
    this.environmentService.retrieveEnvironments().subscribe(envs => {
      console.log(envs)
      this.environments = envs
    });
  }

  goToWorkflow(): void {
    if (this.selectedEnvironmentId !== null) {
      const selectedEnvName = this.environments.find(env => env.environment.eid === this.selectedEnvironmentId)?.environment.name;
      console.log('Navigating to workflow with environment:', selectedEnvName);
      this.activeModal.close(this.selectedEnvironmentId);  // Close the modal and return the selected environment ID
    } else {
      console.log('Creating a new environment.');
      this.activeModal.close(null);  // Close the modal and return null
    }
  }
}
