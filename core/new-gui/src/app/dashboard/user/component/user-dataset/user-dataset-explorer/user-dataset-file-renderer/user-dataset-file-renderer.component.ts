import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from "@angular/core";
import { DatasetService } from "../../../../service/user-dataset/dataset.service";
import { untilDestroyed } from "@ngneat/until-destroy";
import * as Papa from "papaparse";
import { ParseResult } from "papaparse";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";

export const MIME_TYPES = {
  JPEG: "image/jpeg",
  PNG: "image/png",
  CSV: "text/csv",
  TXT: "text/plain",
  MD: "text/markdown",
  HTML: "text/html",
  JSON: "application/json",
  PDF: "application/pdf",
  MSWORD: "application/msword",
  MSEXCEL: "application/vnd.ms-excel",
  MSPOWERPOINT: "application/vnd.ms-powerpoint",
  MP4: "video/mp4",
  MP3: "audio/mpeg",
  OCTET_STREAM: "application/octet-stream", // Default binary format
};

export const MIME_TYPE_SIZE_LIMITS_MB = {
  [MIME_TYPES.JPEG]: 5 * 1024 * 1024, // 5 MB
  [MIME_TYPES.PNG]: 5 * 1024 * 1024, // 5 MB
  [MIME_TYPES.CSV]: 2 * 1024 * 1024, // 2 MB for text-based data files
  [MIME_TYPES.TXT]: 1 * 1024 * 1024, // 1 MB for plain text files
  [MIME_TYPES.HTML]: 1 * 1024 * 1024, // 1 MB for HTML files
  [MIME_TYPES.JSON]: 1 * 1024 * 1024, // 1 MB for JSON files
  [MIME_TYPES.PDF]: 10 * 1024 * 1024, // 10 MB for PDF documents
  [MIME_TYPES.MSWORD]: 10 * 1024 * 1024, // 10 MB for Word documents
  [MIME_TYPES.MSEXCEL]: 10 * 1024 * 1024, // 10 MB for Excel spreadsheets
  [MIME_TYPES.MSPOWERPOINT]: 10 * 1024 * 1024, // 10 MB for PowerPoint presentations
  [MIME_TYPES.MP4]: 50 * 1024 * 1024, // 50 MB for MP4 videos
  [MIME_TYPES.MP3]: 10 * 1024 * 1024, // 10 MB for MP3 audio files
  [MIME_TYPES.OCTET_STREAM]: 5 * 1024 * 1024, // Default size for other binary formats
};

@Component({
  selector: "texera-user-dataset-file-renderer",
  templateUrl: "./user-dataset-file-renderer.component.html",
  styleUrls: ["./user-dataset-file-renderer.component.scss"],
})
export class UserDatasetFileRendererComponent implements OnInit, OnChanges {
  private DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5 MB

  public fileURL: string | undefined;

  // pdf related control
  public displayPdf: boolean = false;

  // csv related control
  public displayCSV: boolean = false;
  public csvHeader: any[] = [];
  public csvContent: any[][] = [];

  // image related control
  public displayImage: boolean = false;
  public imageFileURL: SafeUrl | undefined;

  // markdown control
  public displayMarkdown: boolean = false;

  // plain text & octet stream related control
  public displayPlainText: boolean = false;
  public textContent: string = "";

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
  loadFile = new EventEmitter<{ file: string; prefix: string }>();

  constructor(private datasetService: DatasetService, private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    console.log("init");
    this.reloadFileContent();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.did || changes.dvid || changes.filePath) {
      console.log("on change");
      this.reloadFileContent();
    }
  }

  ngOnDestroy(): void {
    if (this.fileURL) {
      URL.revokeObjectURL(this.fileURL);
    }
  }

  // In your component
  showImageModal = false;
  toggleImageModal() {
    this.showImageModal = !this.showImageModal;
  }

  reloadFileContent() {
    this.turnOffAllDisplay();

    if (this.did && this.dvid && this.filePath != "") {
      this.datasetService
        .retrieveDatasetVersionSingleFile(this.did, this.dvid, this.filePath)
        .pipe()
        .subscribe(
          blob => {
            this.isLoading = true;
            const MaxSize = MIME_TYPE_SIZE_LIMITS_MB[blob.type] || this.DEFAULT_MAX_SIZE;
            const fileSize = blob.size;
            if (fileSize > MaxSize) {
              this.onFileSizeNotLoadable();
              return;
            }
            this.currentFile = new File([blob], this.filePath, { type: blob.type });
            // Handle different file types
            console.log(blob.type);
            switch (blob.type) {
              case MIME_TYPES.PNG:
              case MIME_TYPES.JPEG:
                // Handle image display
                console.log("display image");
                this.imageFileURL = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob));
                this.displayImage = true;
                break;
              case MIME_TYPES.CSV:
                this.displayCSV = true;
                // Handle CSV display
                Papa.parse(this.currentFile, {
                  complete: (results: ParseResult<any>) => {
                    console.log("File Parsed: ", results.data);

                    if (results.data.length > 0) {
                      // Extract the header (first row)
                      this.csvHeader = results.data[0];

                      // Process the rest of the rows
                      this.csvContent = results.data
                        .slice(1)
                        .map(row => {
                          // Normalize the row length to match the header length
                          while (row.length < this.csvHeader.length) {
                            row.push("");
                          }
                          return row;
                        })
                        .filter(row => {
                          // filter out all empty row
                          let areCellAllEmpty = true;
                          for (const cell in row) {
                            if (cell != "") {
                              areCellAllEmpty = false;
                              break;
                            }
                          }
                          return !areCellAllEmpty;
                        });
                    }
                  },
                  error: error => {
                    console.error("Error parsing file:", error);
                    this.onFileLoadingError();
                  },
                });
                break;
              case MIME_TYPES.PDF:
                // Handle PDF display
                console.log("display pdf");
                this.fileURL = URL.createObjectURL(blob);
                setTimeout(() => {
                  this.displayPdf = true;
                  this.isLoading = false;
                }, 0);
                break;
              case MIME_TYPES.MD:
                this.displayMarkdown = true;
                this.readFileAsText(blob);
                break;
              case MIME_TYPES.OCTET_STREAM:
              case MIME_TYPES.TXT:
              default:
                this.displayPlainText = true;
                this.readFileAsText(blob);
                break;
            }

            this.isLoading = false;
          },
          (error: unknown) => {
            console.error("Error fetching file:", error);
            this.onFileLoadingError();
          }
        );
    }
  }

  turnOffAllDisplay() {
    this.displayPdf = false;
    this.displayCSV = false;
    this.displayImage = false;
    this.displayPlainText = false;
    this.isFileLoadingError = false;
    this.displayMarkdown = false;
    this.isLoading = false;
    // garbage collection
    if (this.fileURL) {
      URL.revokeObjectURL(this.fileURL);
    }
    if (this.imageFileURL) {
      URL.revokeObjectURL(this.imageFileURL.toString());
    }
  }

  onFileLoadingError() {
    this.turnOffAllDisplay();
    this.isFileLoadingError = true;
  }

  onFileSizeNotLoadable() {
    this.turnOffAllDisplay();
    this.isFileSizeLoadable = false;
  }

  private readFileAsText(blob: Blob) {
    const txtReader = new FileReader();
    txtReader.onload = (event: any) => {
      this.textContent = event.target.result;
    };
    txtReader.readAsText(blob);
  }
}
