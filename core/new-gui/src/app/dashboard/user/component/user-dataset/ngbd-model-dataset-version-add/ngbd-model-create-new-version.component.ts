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

    onFilesChanged(files: FileUploadItem[]) {
        // Handle the changed files
        this.filesToBeUploaded = files
    }

    public isCreateButtonDisabled(): boolean {
        return this.filesToBeUploaded.every(fileUploadItem => fileUploadItem.isUploadingFlag);
    }

    public onClickCreateVersion(): void {
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
