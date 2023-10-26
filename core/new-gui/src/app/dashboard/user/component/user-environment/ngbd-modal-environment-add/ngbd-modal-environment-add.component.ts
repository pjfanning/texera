import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import { UserService } from "../../../../../common/service/user/user.service";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-environment-add.component',
  templateUrl: './ngbd-modal-environment-add.component.html',
  styleUrls: ['./ngbd-modal-environment-add.component.scss']
})
export class NgbdModalEnvironmentAddComponent implements OnInit {
  validateForm: FormGroup;

  constructor(
    private activeModal: NgbActiveModal,
    private formBuilder: FormBuilder,
    private userService: UserService
  ) {
    this.validateForm = this.formBuilder.group({
      name: ["Untitled Environment"],
      description: [""],
      isPublic: [0],
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onSubmitAddEnvironment(): void {
  }
}
