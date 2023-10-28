import { UntilDestroy } from "@ngneat/until-destroy";
import { Component, OnInit } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { EnvironmentService } from "../../../service/user-environment/environment.service";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-workflow-environment-select.component',
  templateUrl: './ngbd-modal-workflow-environment-select.component.html',
  styleUrls: ['./ngbd-modal-workflow-environment-select.component.scss']
})
export class NgbdModalWorkflowEnvironmentSelectComponent implements OnInit {
  selectedEnvironmentId: number | null = null;
  environments: {id: number, name: string}[] = [];

  constructor(
    private activeModal: NgbActiveModal,
    private environmentService: EnvironmentService) {}

  ngOnInit(): void {
    const environmentIdentifiers = this.environmentService.getEnvironmentIdentifiers();
    this.environments = Array.from(environmentIdentifiers.entries()).map(([id, name]) => ({ id, name }));
  }

  goToWorkflow(): void {
    if (this.selectedEnvironmentId !== null) {
      const selectedEnvName = this.environments.find(env => env.id === this.selectedEnvironmentId)?.name;
      console.log('Navigating to workflow with environment:', selectedEnvName);
      this.activeModal.close(this.selectedEnvironmentId);  // Close the modal and return the selected environment ID
    } else {
      console.log('Creating a new environment.');
      this.activeModal.close(null);  // Close the modal and return null
    }
  }
}
