import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../../service/user-dataset/dataset.service";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";

@UntilDestroy()
@Component({
  templateUrl: "./ngbd-model-dataset-file-add.component.html",
  styleUrls: ["./ngbd-model-dataset-file-add.component.scss"]
})
export class NgbdModelDatasetFileAddComponent implements OnInit {
  versionNotes: string = '';

  constructor(
    public activeModal: NgbActiveModal
  ) {}

  ngOnInit(): void {
      
  }
}