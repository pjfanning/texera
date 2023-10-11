import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'ngbd-modal-dataset-add.component',
  templateUrl: './ngbd-modal-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-dataset-add.component.scss']
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
