import {UntilDestroy} from "@ngneat/until-destroy";
import {Component, OnInit} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-workflow-environment-select.component',
  templateUrl: './ngbd-modal-workflow-environment-select.component.html',
  styleUrls: ['./ngbd-modal-workflow-environment-select.component.scss']
})
export class NgbdModalWorkflowEnvironmentSelectComponent implements OnInit {
  selectedEnvironment: string | null = null;
  existingEnvironments: string[] = [
    // Example environments, replace with your actual data.
    'Environment 1',
    'Environment 2',
    'Environment 3'
  ];

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
  }

  goToWorkflow(): void {
    if (this.selectedEnvironment !== null) {
      // Your logic to navigate or do anything else with the selected environment
      console.log('Navigating to workflow with environment:', this.selectedEnvironment);
      this.activeModal.close(this.selectedEnvironment);  // Close the modal and return the selected environment
    } else {
      console.log('Creating a new environment.');
      this.activeModal.close(null);  // Close the modal and return null
    }
  }
}
