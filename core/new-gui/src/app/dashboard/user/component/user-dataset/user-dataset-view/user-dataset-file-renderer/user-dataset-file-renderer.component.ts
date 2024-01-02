import {Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {DatasetService} from "../../../../service/user-dataset/dataset.service";
import {untilDestroyed} from "@ngneat/until-destroy";
import * as Papa from 'papaparse';
import {ParseResult} from "papaparse";

export const MIME_TYPES = {
  JPEG: 'image/jpeg',
  PNG: 'image/png',
  CSV: 'text/csv',
  TXT: 'text/plain',
  HTML: 'text/html',
  JSON: 'application/json',
  PDF: 'application/pdf',
  MSWORD: 'application/msword',
  MSEXCEL: 'application/vnd.ms-excel',
  MSPOWERPOINT: 'application/vnd.ms-powerpoint',
  MP4: 'video/mp4',
  MP3: 'audio/mpeg',
  OCTET_STREAM: 'application/octet-stream' // Default binary format
};

export const MIME_TYPE_SIZE_LIMITS_MB = {
  [MIME_TYPES.JPEG]: 5 * 1024 * 1024,  // 5 MB
  [MIME_TYPES.PNG]: 5 * 1024 * 1024,   // 5 MB
  [MIME_TYPES.CSV]: 2 * 1024 * 1024,   // 2 MB for text-based data files
  [MIME_TYPES.TXT]: 1 * 1024 * 1024,   // 1 MB for plain text files
  [MIME_TYPES.HTML]: 1 * 1024 * 1024,  // 1 MB for HTML files
  [MIME_TYPES.JSON]: 1 * 1024 * 1024,  // 1 MB for JSON files
  [MIME_TYPES.PDF]: 10 * 1024 * 1024,  // 10 MB for PDF documents
  [MIME_TYPES.MSWORD]: 10 * 1024 * 1024,  // 10 MB for Word documents
  [MIME_TYPES.MSEXCEL]: 10 * 1024 * 1024,  // 10 MB for Excel spreadsheets
  [MIME_TYPES.MSPOWERPOINT]: 10 * 1024 * 1024,  // 10 MB for PowerPoint presentations
  [MIME_TYPES.MP4]: 50 * 1024 * 1024,  // 50 MB for MP4 videos
  [MIME_TYPES.MP3]: 10 * 1024 * 1024,  // 10 MB for MP3 audio files
  [MIME_TYPES.OCTET_STREAM]: 5 * 1024 * 1024  // Default size for other binary formats
};

@Component({
  selector: 'texera-user-dataset-file-renderer',
  templateUrl: './user-dataset-file-renderer.component.html',
  styleUrls: ['./user-dataset-file-renderer.component.scss']
})
export class UserDatasetFileRendererComponent implements OnInit, OnChanges {
  private DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5 MB

  public fileURL: string = "";

  // pdf related control
  public displayPdf: boolean = false;

  // csv related control
  public displayCSV: boolean = false;
  public csvTitle: any[] = [];
  public csvContent: any[] = [];

  // image related control
  public displayImage: boolean = false;

  public isLoading: boolean = false;
  public isFileSizeLoadable = true;
  public currentFile: File | undefined = undefined;

  public isFileLoadingError: boolean = false;

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

  ngOnDestroy(): void {
    if (this.fileURL) {
      URL.revokeObjectURL(this.fileURL);
    }
  }


  reloadFileContent() {
    console.log(this.did, this.dvid, this.filePath);
    if (this.did && this.dvid && this.filePath != "") {
      this.datasetService
        .retrieveDatasetVersionSingleFile(this.did, this.dvid, this.filePath)
        .pipe()
        .subscribe(blob => {
            this.isLoading = true;
            const MaxSize = MIME_TYPE_SIZE_LIMITS_MB[blob.type] || this.DEFAULT_MAX_SIZE;
            const fileSize = blob.size;
            if (fileSize > MaxSize) {
              this.onFileSizeNotLoadable()
              return;
            }
            this.currentFile = new File([blob], this.filePath, {type: blob.type});
            this.turnOffAllDisplay();

            // Handle different file types
            switch (blob.type) {
              case MIME_TYPES.PNG:
              case MIME_TYPES.JPEG:
                // Handle image display
                this.fileURL = URL.createObjectURL(blob);
                this.displayImage = true;
                break;
              case MIME_TYPES.CSV:
                this.displayCSV = true;
                // Handle CSV display
                Papa.parse(this.currentFile, {
                  complete: (results) => {
                    console.log('File Parsed: ', results.data);
                  },
                  error: (error) => {
                    console.error('Error parsing file:', error);
                  }
                });
                break;
              case MIME_TYPES.PDF:
                // Handle PDF display
                console.log("display pdf")
                setTimeout(() => {
                  this.displayPdf = true;
                  this.isLoading = false;
                }, 0);
                break;
              // Add more cases as needed for different file types
              default:
                // Handle unknown file type or default behavior
                break;
            }

            this.isLoading = false;
          }
          , error => {
            console.error("Error fetching file:", error);
            this.isLoading = false;
            this.onFileLoadingError()
            // Handle error
          }
        );
    }
  }


  turnOffAllDisplay() {
    this.displayPdf = false;
    this.displayCSV = false;
  }

  onFileLoadingError() {
    this.isLoading = false;
    this.isFileLoadingError = true
  }

  onFileSizeNotLoadable() {
    this.isLoading = false;
    this.isFileSizeLoadable = false;
  }
}
