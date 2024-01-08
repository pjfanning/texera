import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { FormlyFieldConfig } from "@ngx-formly/core";
import { DatasetService } from "../../../../service/user-dataset/dataset.service";
import { FileUploadItem } from "../../../../type/dashboard-file.interface";
import { Dataset, DatasetVersion } from "../../../../../../common/type/dataset";
import { untilDestroyed } from "@ngneat/until-destroy";

@Component({
  selector: "texera-user-dataset-version-creator",
  templateUrl: "./user-dataset-version-creator.component.html",
  styleUrls: ["./user-dataset-version-creator.component.scss"],
})
export class UserDatasetVersionCreator implements OnInit {
  @Input()
  isCreatingVersion: boolean = false;

  @Input()
  baseVersion: DatasetVersion | undefined;

  // this emits the ID of the newly created version/dataset, will emit 0 if creation is failed.
  @Output()
  datasetOrVersionCreationID: EventEmitter<number> = new EventEmitter<number>();

  isCreateButtonDisabled: boolean = false;

  newUploadFiles: FileUploadItem[] = [];

  removedFilePaths: string[] = [];

  form = new FormGroup({});
  model: any = {};
  fields: FormlyFieldConfig[] = [];
  isDatasetPublic: boolean = true;

  constructor(private datasetService: DatasetService) {}

  ngOnInit() {
    this.setFormFields();
  }

  ngOnChanges() {
    // Update the form fields when the input property changes
    this.setFormFields();
  }

  private setFormFields() {
    this.fields = this.isCreatingVersion
      ? [
          // Fields when isCreatingVersion is true
          {
            key: "name",
            type: "input",
            templateOptions: {
              label: "Name",
              required: true,
            },
          },
        ]
      : [
          // Fields when isCreatingVersion is false
          {
            key: "name",
            type: "input",
            templateOptions: {
              label: "Name",
              required: true,
            },
          },
          {
            key: "description",
            type: "input",
            templateOptions: {
              label: "Description",
            },
          },
          {
            key: "versionName",
            type: "input",
            templateOptions: {
              label: "Initial Version Name",
              required: true,
            },
          },
        ];
  }


  private triggerValidation() {
    Object.keys(this.form.controls).forEach(field => {
      const control = this.form.get(field);
      control?.markAsTouched({ onlySelf: true });
    });
  }

  onClickCancel() {
    this.datasetOrVersionCreationID.emit(0);
  }

  onClickCreate() {
    // check if the form is valid
    this.triggerValidation();

    if (!this.form.valid) {
      return; // Stop further execution if the form is not valid
    }

    if (this.isCreatingVersion && this.baseVersion) {
      console.log("creating a version")
      const versionName = this.form.get("name")?.value;
      this.datasetService
        .createDatasetVersion(this.baseVersion?.did, versionName, this.removedFilePaths, this.newUploadFiles)
        .pipe()
        .subscribe(res => {
          console.log("version creation succeed");
          this.datasetOrVersionCreationID.emit(res.dvid);
        });
    } else {
      const ds: Dataset = {
        name: this.form.get("name")?.value,
        description: this.form.get("description")?.value,
        isPublic: this.isDatasetPublic ? 1 : 0,
        did: undefined,
        storagePath: undefined,
        creationTime: undefined,
        versionHierarchy: undefined,
      };
      const initialVersionName = this.form.get("versionName")?.value;
      this.datasetService
        .createDataset(ds, initialVersionName, this.newUploadFiles)
        .pipe()
        .subscribe(res => {
          this.datasetOrVersionCreationID.emit(res.dataset.did);
        });
    }
  }

  onNewUploadFilesChanged(files: FileUploadItem[]) {
    this.newUploadFiles = files;
  }

  onRemovingFilePathsChanged(paths: string[]) {
    this.removedFilePaths = this.removedFilePaths.concat(paths);
  }
}
