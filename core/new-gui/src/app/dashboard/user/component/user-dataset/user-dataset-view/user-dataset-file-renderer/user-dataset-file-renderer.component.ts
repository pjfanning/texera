import {Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {DatasetService} from "../../../../service/user-dataset/dataset.service";
import {untilDestroyed} from "@ngneat/until-destroy";
import {FileSizeLimits} from "../../../../../../common/type/datasetVersion";


@Component({
    selector: 'texera-user-dataset-file-renderer',
    templateUrl: './user-dataset-file-renderer.component.html',
    styleUrls: ['./user-dataset-file-renderer.component.scss']
})
export class UserDatasetFileRendererComponent implements OnInit, OnChanges {
    private FILE_SIZE_LIMITS: FileSizeLimits = {
        ".pdf": 15 * 1024 * 1024, // 15 MB
        ".csv": 2 * 1024 * 1024,    // 2 MB
    };
    private DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    public fileURL: string = "";
    public csvContent: any[] = [];
    public pdfDisplay: boolean = false;
    public csvDisplay: boolean = false;
    public isLoading: boolean = false;
    public isFileSizeLoadable = true;
    public currentFileBlob: Blob | undefined = undefined;
    public currentFileName: string = "";

    @Input()
    isMaximized: boolean = false;

    @Input()
    did: number | undefined;

    @Input()
    dvid: number | undefined;

    @Input()
    filePath: string = "";

    @Output()
    loadFile = new EventEmitter<{ file: string, prefix: string }>();

    constructor(
        private datasetService: DatasetService,
    ) {
    }

    ngOnInit(): void {
        console.log("init")
        this.reloadFileContent()
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.did || changes.dvid || changes.filePath) {
            console.log("on change")
            this.reloadFileContent();
        }
    }

    reloadFileContent() {
        console.log(this.did, this.dvid, this.filePath)
        if (this.did && this.dvid && this.filePath != "") {
            console.log("enter the block")
            this.datasetService
                .inspectDatasetSingleFile(this.did, this.dvid, this.filePath)
                .pipe()
                .subscribe(blob => {
                    this.isLoading = true;
                    this.currentFileBlob = new File([blob], this.filePath, {type: blob.type});
                    this.fileURL = URL.createObjectURL(blob);
                    console.log("file receive: ", this.fileURL)

                    const lastDotIndex = this.filePath.lastIndexOf('.');
                    const fileExtension = lastDotIndex !== -1 ? this.filePath.slice(lastDotIndex) : '';
                    const MaxSize = this.FILE_SIZE_LIMITS[fileExtension] || this.DEFAULT_MAX_SIZE;

                    const fileSize = blob.size

                    if (fileSize > MaxSize) {
                        this.isFileSizeLoadable = false;
                        return;
                    }

                    if (this.filePath.endsWith(".pdf")) {
                        this.pdfDisplay = false;
                        setTimeout(() => {
                            this.pdfDisplay = true;
                            this.isLoading = false;
                        }, 0);
                    } else if (this.filePath.endsWith(".csv")) {
                        //   Papa.parse(this.currentFileObject, {
                        //     complete: (results) => {
                        //         this.csvContent = results.data;
                        //         this.csvDisplay = true;
                        //         this.isLoading = false;
                        //     }
                        // });
                    } else {
                        this.turnOffAllDisplay();
                        this.isLoading = false;
                    }
                })
        }
    }

    turnOffAllDisplay() {
        this.pdfDisplay = false;
        this.csvDisplay = false;
    }
}
