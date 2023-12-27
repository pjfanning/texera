import {OnInit, Component, Output, EventEmitter} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FileUploader} from "ng2-file-upload";
import {FileUploadItem} from "../../../type/dashboard-file.interface";
import {UserFileUploadService} from "../../../service/user-file/user-file-upload.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DatasetVersion, DatasetVersionHierarchyNode} from "../../../../../common/type/datasetVersion";


@UntilDestroy()
@Component({
    templateUrl: "./ngbd-model-create-new-version.component.html",
    styleUrls: ["./ngbd-model-create-new-version.component.scss"]
})
export class NgbdModelCreateNewVersion implements OnInit {
    public versionName: string = '';
    public haveDropZoneOver: boolean = false;
    public uploader: FileUploader = new FileUploader({url: ""});
    public baseVersion: DatasetVersion | undefined;
    public removedOldFiles: string[] = [];


    private filesToBeUploaded: FileUploadItem[] = [];
    private did: number | undefined;

    @Output() versionAdded = new EventEmitter<void>();

    constructor(
        public activeModal: NgbActiveModal,
        private datasetService: DatasetService
    ) {

    }

    ngOnInit(): void {
        if (this.did) {
            this.datasetService.retrieveDatasetLatestVersion(this.did)
                .pipe(untilDestroyed(this))
                .subscribe(version => {
                    this.baseVersion = version;
                })
        }
    }

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
      if (this.baseVersion) {
        this.baseVersion.versionHierarchyRoot = this.baseVersion.versionHierarchyRoot
            ? this.baseVersion.versionHierarchyRoot.filter(file => file !== removedFile) : undefined;
        if (removedFile.dir) {
          this.removedOldFiles.push(removedFile.dir + "/" + removedFile.name);
        } else {
          this.removedOldFiles.push(removedFile.name)
        }
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
      if (this.did) {
        let files = new Array(this.filesToBeUploaded.length);

        for (let i = 0; i < this.filesToBeUploaded.length; i++) {
          this.filesToBeUploaded[i].isUploadingFlag = true;
          files[i] = this.filesToBeUploaded[i].file
        }

        this.datasetService
            .createDatasetVersion(this.did, this.versionName, this.removedOldFiles, files)
            .pipe(untilDestroyed(this))
            .subscribe(() => {
              this.filesToBeUploaded = [];
              this.activeModal.dismiss('Cross click');
              this.onVersionAdd();
            })
      }
    }

    onVersionAdd() {
        this.versionAdded.emit();
    }

}
