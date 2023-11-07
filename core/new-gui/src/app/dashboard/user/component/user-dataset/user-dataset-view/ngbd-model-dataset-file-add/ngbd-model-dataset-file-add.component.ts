import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { FileUploader } from "ng2-file-upload";
import { FileUploadItem } from "../../../../type/dashboard-file.interface";
import { UserFileUploadService } from "../../../../service/user-file/user-file-upload.service";
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
  public haveDropZoneOver: boolean = false;

  public uploader: FileUploader = new FileUploader({ url: "" });

  constructor(
    public activeModal: NgbActiveModal, private userFileUploadService: UserFileUploadService
  ) {}

  ngOnInit(): void {}

  // will be replaced by new service function
  
  public getFileArray(): ReadonlyArray<Readonly<FileUploadItem>> {
    return this.userFileUploadService.getFilesToBeUploaded();
  }

  public getFileArrayLength(): number {
    return this.userFileUploadService.getFilesToBeUploaded().length;
  }

  public deleteFile(fileUploadItem: FileUploadItem): void {
    this.userFileUploadService.removeFileFromUploadArray(fileUploadItem);
  }

  public uploadAllFiles(): void {
    this.userFileUploadService.uploadAllFiles();
  }

  public isUploadAllButtonDisabled(): boolean {
    return this.userFileUploadService.getFilesToBeUploaded().every(fileUploadItem => fileUploadItem.isUploadingFlag);
  }

  public haveFileOver(fileOverEvent: boolean): void {
    this.haveDropZoneOver = fileOverEvent;
  }

  public getFileDropped(fileDropEvent: File[]): void {
    for (let i = 0; i < fileDropEvent.length; i++) {
      const file: File | null = fileDropEvent[i];
      if (file !== null) {
        this.userFileUploadService.addFileToUploadArray(file);
      }
    }

    this.uploader.clearQueue();
  }

  public handleClickUploadFile(clickUploadEvent: Event): void {
    const fileList: FileList | null = (clickUploadEvent as any).target.files;
    if (fileList === null) {
      throw new Error("browser upload does not work as intended");
    }

    for (let i = 0; i < fileList.length; i++) {
      this.userFileUploadService.addFileToUploadArray(fileList[i]);
    }
  }

}
