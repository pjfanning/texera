import { OnInit, Component, EventEmitter, Output } from "@angular/core";
import { FormBuilder, FormGroup } from "@angular/forms";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { Dataset } from "../../../../../common/type/dataset";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { UserService } from "../../../../../common/service/user/user.service";
import { FileUploadItem } from "../../../type/dashboard-file.interface";
import { UserFileUploadService } from "../../../service/user-file/user-file-upload.service";
import { FileUploader } from "ng2-file-upload";

@UntilDestroy()
@Component({
  selector: "ngbd-modal-dataset-add.component",
  templateUrl: "./ngbd-modal-dataset-add.component.html",
  styleUrls: ["./ngbd-modal-dataset-add.component.scss"],
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
      datasetName: ["Untitled Dataset"],
      datasetDescription: [""],
      initialVersionName: ["Version 1"],
      isDatasetPublic: [0],
    });
  }

  ngOnInit(): void {}

  close(): void {
    this.activeModal.close();
  }

  onFilesChanged(files: FileUploadItem[]) {
    // Handle the changed files
    this.filesToBeUploaded = files;
  }

  onSubmitAddDataset(): void {
    console.log("onSubmit trigger");
    const ds: Dataset = {
      name: this.validateForm.get("datasetName")?.value,
      description: this.validateForm.get("datasetDescription")?.value,
      isPublic: this.validateForm.get("isDatasetPublic")?.value,
      did: undefined,
      storagePath: undefined,
      creationTime: undefined,
      versionHierarchy: undefined,
    };

    const initialVersionName = this.validateForm.get("initialVersionName")?.value;

    // this.datasetService
    //   .createDataset(ds, initialVersionName, this.filesToBeUploaded)
    //   .pipe(untilDestroyed(this))
    //   .subscribe({
    //     next: value => {
    //       console.log("Dataset Creation succeeded");
    //       this.datasetAdded.emit(); // Emit the event after successful addition
    //     },
    //     error: (err: unknown) => alert(JSON.stringify(err.error)),
    //     complete: () => this.activeModal.close(),
    //   });
  }
}
