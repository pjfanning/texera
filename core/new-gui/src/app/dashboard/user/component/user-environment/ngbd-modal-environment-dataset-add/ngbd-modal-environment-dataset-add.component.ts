import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { UntilDestroy } from "@ngneat/until-destroy";
import { EnvironmentService } from "../../../service/user-environment/environment.service";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-environment-dataset-add.component',
  templateUrl: './ngbd-modal-environment-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-environment-dataset-add.component.scss']
})
export class NgbdModalEnvironmentDatasetAddComponent implements OnInit {
  validateForm: FormGroup;

  constructor(
      private activeModal: NgbActiveModal,
      private formBuilder: FormBuilder,
      private environmentService: EnvironmentService
  ) {
    this.validateForm = this.formBuilder.group({
      did: [null]
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onSubmitLinkDatasetToEnvironment(): void {
    const did = this.validateForm.get("did")?.value;
    // Ensure did is treated as a number
    const didNumber = did ? parseInt(did, 10) : null;
    this.activeModal.close(didNumber);
  }
}
