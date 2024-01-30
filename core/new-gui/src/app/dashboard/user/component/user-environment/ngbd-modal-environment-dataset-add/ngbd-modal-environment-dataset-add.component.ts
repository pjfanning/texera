import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { EnvironmentService } from "../../../service/user-environment/environment.service";
import { DashboardDataset } from "../../../type/dashboard-dataset.interface";
import { DatasetService } from "../../../service/user-dataset/dataset.service";

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-environment-dataset-add.component',
  templateUrl: './ngbd-modal-environment-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-environment-dataset-add.component.scss']
})
export class NgbdModalEnvironmentDatasetAddComponent implements OnInit {
  dashboardDatasets: DashboardDataset[] = [];
  filteredOptions: string[] = [];
  inputValue?: string; // Used for ngModel binding

  constructor(
      private activeModal: NgbActiveModal,
      private environmentService: EnvironmentService,
      private datasetService: DatasetService,
  ) {}

  ngOnInit(): void {
    this.datasetService.retrieveAccessibleDatasets()
        .pipe(untilDestroyed(this))
        .subscribe({
          next: datasets => {
            this.dashboardDatasets = datasets;
          }
        });
  }

  onSearch(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.filteredOptions = this.dashboardDatasets
        .map(d => d.dataset.name)
        .filter(name => name.toLowerCase().includes(value.toLowerCase()));
  }

  close(): void {
    this.activeModal.dismiss();
  }

  onSubmitLinkDatasetToEnvironment(): void {
    const selectedDataset = this.dashboardDatasets.find(d => d.dataset.name === this.inputValue);
    const didNumber = selectedDataset?.dataset.did;
    this.activeModal.close(didNumber);
  }
}
