import { OnInit, Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { Dataset } from "../../../../../common/type/dataset";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import { UserService } from "../../../../../common/service/user/user.service";
import { FileUploadItem } from '../../../type/dashboard-file.interface';
import { UserFileUploadService } from '../../../service/user-file/user-file-upload.service';
import { FileUploader } from 'ng2-file-upload';

@UntilDestroy()
@Component({
  selector: 'ngbd-modal-dataset-add.component',
  templateUrl: './ngbd-modal-dataset-add.component.html',
  styleUrls: ['./ngbd-modal-dataset-add.component.scss']
})
export class NgbdModalDatasetAddComponent implements OnInit {
  validateForm: FormGroup;

  haveDropZoneOver: boolean = false;
  private filesToBeUploaded: FileUploadItem[] = [];
  public uploader: FileUploader = new FileUploader({ url: "" });

  @Output() datasetAdded = new EventEmitter<void>();
  @Output() versionAdded = new EventEmitter<void>();

  constructor(
    private activeModal: NgbActiveModal,
    private datasetService: DatasetService,
    private formBuilder: FormBuilder,
    private userService: UserService
  ) {
    this.validateForm = this.formBuilder.group({
      name: ["Untitled Dataset"],
      baseVersion: ["baseVersion"],
      description: [""],
      isPublic: [0],
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onSubmitAddDataset(): void {
    const ds: Dataset = {
      name: this.validateForm.get('name')?.value,
      description: this.validateForm.get('description')?.value,
      isPublic: this.validateForm.get('isPublic')?.value,
      did: undefined,
      storagePath: undefined,
      creationTime: undefined,
      versionHierarchy: undefined,
    }

    this.datasetService.createDataset(ds)
      .pipe(untilDestroyed(this))
      .subscribe({
        next: value => {
          console.log("Dataset Creation succeeded");
          this.datasetAdded.emit(); // Emit the event after successful addition
        },
        error: (err) => alert(JSON.stringify(err.error)),
        complete: () => this.activeModal.close()
      });
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

  public getFileArrayLength(): number {
    return this.filesToBeUploaded.length;
  }

  public getFileArray(): FileUploadItem[] {
    return this.filesToBeUploaded;
  }

  public deleteNewFile(removedFile: FileUploadItem): void {
    this.filesToBeUploaded = this.filesToBeUploaded.filter(file => file !== removedFile);
  }
}
