import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { FormlyFieldConfig } from '@ngx-formly/core';
import {DatasetService} from "../../../../service/user-dataset/dataset.service";
import {DatasetVersion} from "../../../../../../common/type/datasetVersionFileTree";
import {FileUploadItem} from "../../../../type/dashboard-file.interface";

@Component({
    selector: 'texera-dataset-version-creator',
    templateUrl: './user-dataset-version-creator.component.html',
    styleUrls: ['./user-dataset-version-creator.component.scss']
})
export class UserDatasetVersionCreator implements OnInit {
    @Input()
    isCreatingVersion: boolean = false;

    @Input()
    baseVersion: DatasetVersion | undefined;

    isCreateButtonDisabled: boolean = false;

    form = new FormGroup({});
    model: any = {};
    fields: FormlyFieldConfig[] = [];

    constructor(private datasetService: DatasetService) {
    }

    ngOnInit() {
        this.setFormFields();
    }

    ngOnChanges() {
        // Update the form fields when the input property changes
        this.setFormFields();
    }

    private setFormFields() {
        this.fields = this.isCreatingVersion ? [
            // Fields when isCreatingVersion is true
            {
                key: 'name',
                type: 'input',
                templateOptions: {
                    label: 'Name',
                    required: true
                }
            }
        ] : [
            // Fields when isCreatingVersion is false
            {
                key: 'name',
                type: 'input',
                templateOptions: {
                    label: 'Name',
                    required: true
                }
            },
            {
                key: 'description',
                type: 'input',
                templateOptions: {
                    label: 'Description'
                }
            },
            {
                key: 'versionName',
                type: 'input',
                templateOptions: {
                    label: 'Version Name',
                    required: true
                }
            },
            {
                key: 'accessLevel',
                type: 'toggle',
                templateOptions: {
                    label: 'Dataset Access Level',
                    options: [
                        { value: 'private', label: 'Private' },
                        { value: 'public', label: 'Public' }
                    ]
                }
            }
        ];
    }

    onClickCreate() {

    }

    onFilesChanged(files: FileUploadItem[]) {

    }

}
