import {OnInit, Component, Output, EventEmitter} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FileUploader} from "ng2-file-upload";
import {FileUploadItem} from "../../../type/dashboard-file.interface";
import {UserFileUploadService} from "../../../service/user-file/user-file-upload.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DatasetVersion, DatasetVersionFileTreeNode} from "../../../../../common/type/datasetVersion";


@UntilDestroy()
@Component({
    templateUrl: "./ngbd-model-dataset-version-add.component.html",
    styleUrls: ["./ngbd-model-dataset-version-add.component.scss"]
})
export class NgbdModelDatasetVersionAddComponent implements OnInit {
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
        this.datasetService
            .createDatasetVersion(this.did, this.versionName, this.removedOldFiles, this.filesToBeUploaded)
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
