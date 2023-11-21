import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { FileUploader } from "ng2-file-upload";
import { FileUploadItem } from "../../../../type/dashboard-file.interface";
import { UserFileUploadService } from "../../../../service/user-file/user-file-upload.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../../service/user-dataset/dataset.service";
import { NgbActiveModal, NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { DatasetVersionHierarchyNode } from "src/app/common/type/datasetVersion";


@UntilDestroy()
@Component({
  templateUrl: "./ngbd-model-dataset-file-add.component.html",
  styleUrls: ["./ngbd-model-dataset-file-add.component.scss"]
})
export class NgbdModelDatasetFileAddComponent implements OnInit {
  public versionName: string = '';
  public haveDropZoneOver: boolean = false;
  public uploader: FileUploader = new FileUploader({ url: "" });
  public baseVersion: string = "";
  public existedVersions: string[] = [];
  public baseVersionFiles: DatasetVersionHierarchyNode[] = [];
  public removedOldFiles: string = "";

  private filesToBeUploaded: FileUploadItem[] = [];
  private did: number = 0;

  constructor(
    public activeModal: NgbActiveModal, private datasetService: DatasetService
  ) {}

  ngOnInit(): void {}

  // will be replaced by new service function
  
  public getFileArray(): FileUploadItem[] {
    return this.filesToBeUploaded;
  }

  public getFileArrayLength(): number {
    return this.filesToBeUploaded.length;
  }

  public deleteNewFile(removedFile: FileUploadItem): void {
    this.filesToBeUploaded = this.filesToBeUploaded.filter(file => file !== removedFile);
  }

  public deleteOldFile(removedFile: DatasetVersionHierarchyNode): void {
    this.baseVersionFiles = this.baseVersionFiles.filter(file => file !== removedFile);
    if (this.removedOldFiles !== ""){
      this.removedOldFiles = this.removedOldFiles + "," + removedFile.dir + "/" + removedFile.name;
    } else {
      this.removedOldFiles = removedFile.dir + "/" + removedFile.name;
    }
  }

  public isCreateButtonDisabled(): boolean {
    return this.filesToBeUploaded.every(fileUploadItem => fileUploadItem.isUploadingFlag); 
  }

  public haveFileOver(fileOverEvent: boolean): void {
    this.haveDropZoneOver = fileOverEvent;
  }

  public getFileDropped(fileDropEvent: File[]): void {
    for (let i = 0; i < fileDropEvent.length; i++) {
      const file: File | null = fileDropEvent[i];
      if (file !== null) {
        this.filesToBeUploaded.push(UserFileUploadService.createFileUploadItem(file));
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
      this.filesToBeUploaded.push(UserFileUploadService.createFileUploadItem(fileList[i]));
    }
  }

  public createVersion(): void {
    let files = new Array(this.filesToBeUploaded.length);

    for (let i = 0; i < this.filesToBeUploaded.length; i++) {
      this.filesToBeUploaded[i].isUploadingFlag = true;
      files[i] = this.filesToBeUploaded[i].file
    }

    this.datasetService
    .createDatasetVersion(this.did, this.baseVersion, this.versionName, this.removedOldFiles, files)
    .pipe(untilDestroyed(this))
    .subscribe(() => {
      this.filesToBeUploaded = [];
      this.activeModal.dismiss('Cross click');
    })

  }

  public onBaseVersionSelected(versionName: string) {
    this.baseVersion = versionName;
    this.baseVersionFiles = [];

    this.datasetService
    .retrieveDatasetVersionFileHierarchy(this.did, this.baseVersion)
    .pipe(untilDestroyed(this))
    .subscribe(hierarchyList => {
      hierarchyList.forEach(node => this.iterateFilesInBaseVersion(node))
    })
  }

  public iterateFilesInBaseVersion(node: DatasetVersionHierarchyNode): void {
      if (node.type === "file") {
        this.baseVersionFiles.push(node);
      } else {
        node.children?.forEach(subNode => {
          this.iterateFilesInBaseVersion(subNode);
        })
      }
  }

}
