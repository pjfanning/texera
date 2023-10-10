import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-create-dataset-modal',
  templateUrl: './create-dataset-modal.component.html',
  styleUrls: ['./create-dataset-modal.component.scss']
})
export class NgbdModalDatasetAddComponent implements OnInit {
  dataset = {
    name: '',
    description: '',
    isPublic: false
  };

  constructor(public activeModal: NgbActiveModal) { }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  submit(): void {
    // Handle your form submission here
    console.log(this.dataset);
    this.activeModal.close(this.dataset);
  }
}
