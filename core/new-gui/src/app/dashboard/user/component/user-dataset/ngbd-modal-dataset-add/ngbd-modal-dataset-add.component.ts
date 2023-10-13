import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { Dataset } from "../../../../../common/type/dataset";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import { UserService } from "../../../../../common/service/user/user.service";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-dataset-add.component',
  templateUrl: './ngbd-modal-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-dataset-add.component.scss']
})
export class NgbdModalDatasetAddComponent implements OnInit {
  validateForm: FormGroup;

  constructor(
    private activeModal: NgbActiveModal,
    private datasetService: DatasetService,
    private formBuilder: FormBuilder,
    private userService: UserService
  ) {
    this.validateForm = this.formBuilder.group({
      name: ["Untitled Dataset"],
      description: [""],
      isPublic: [0],
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onSubmitAddDataset(): void {
    const ds: Dataset = {
      name: this.validateForm.get('name')?.value,
      description: this.validateForm.get('description')?.value,
      isPublic: this.validateForm.get('isPublic')?.value,
      did: undefined,
      storagePath: undefined,
      creationTime: undefined
    }

    this.datasetService.createDataset(ds)
      .pipe(untilDestroyed(this))
      .subscribe({
        next: value => console.log("Dataset Creation succeeded"),
        error: (err) => alert(err.error),
        complete: () => this.activeModal.close()
      });
  }
}
