import {Component, Input, OnInit} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import { UserService } from "../../../../../common/service/user/user.service";
import {EnvironmentService} from "../../../service/user-environment/environment.service";
import {DashboardEnvironment} from "../../../type/environment";

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
      name: [""],
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

   onSubmitAddDatasetToEnvironment(): void {
     const dsName = this.validateForm.get("name")?.value;
     this.activeModal.close(dsName);
  }
}
