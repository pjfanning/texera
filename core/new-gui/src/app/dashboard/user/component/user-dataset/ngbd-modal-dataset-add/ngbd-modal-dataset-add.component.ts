import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {Dataset} from "../../../../../common/type/dataset";
import {untilDestroyed} from "@ngneat/until-destroy";

@Component({
  selector: 'ngbd-modal-dataset-add.component',
  templateUrl: './ngbd-modal-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-dataset-add.component.scss']
})
export class NgbdModalDatasetAddComponent implements OnInit {
  dataset = {
    name: 'Untitled Dataset',
    description: '',
    isPublic: false
  };

  constructor(
    private activeModal: NgbActiveModal,
    private datasetService: DatasetService
  ) {}

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onSubmitAddDataset(): void {
    // Handle your form submission here
    const dataset: Dataset = {
      name: this.dataset.name,
      description: this.dataset.description,
      is_public: Number(this.dataset.isPublic),
      did: undefined,
      storage_path: undefined,
      creation_time: undefined
    }
    this.datasetService.createDataset(dataset)
      .pipe()
      .subscribe({
        next: value => console.log("Dataset Creation succeed"),
        error: (err) => alert(err.error),
      })
    this.activeModal.close(this.dataset);
  }
}
