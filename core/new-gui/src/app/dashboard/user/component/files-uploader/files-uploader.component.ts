import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FileUploadItem } from "../../type/dashboard-file.interface";
import { NgxFileDropEntry } from "ngx-file-drop";
import { UserFileUploadService } from "../../service/user-file/user-file-upload.service";
import {
  DatasetVersionFileTreeManager,
  DatasetVersionFileTreeNode,
  getFullPathFromFileTreeNode,
  getPathsFromNode,
  parseFileUploadItemToVersionFileTreeNodes,
} from "../../../../common/type/datasetVersionFileTree";

@Component({
  selector: "texera-user-files-uploader",
  templateUrl: "./files-uploader.component.html",
  styleUrls: ["./files-uploader.component.scss"],
})
export class FilesUploaderComponent {
  @Input()
  public previouslyUploadFiles: DatasetVersionFileTreeNode[] | undefined;

  @Output()
  uploadedFiles = new EventEmitter<FileUploadItem[]>();

  @Output()
  removingFilePaths = new EventEmitter<string[]>();

  newUploadFiles: Map<string, FileUploadItem> = new Map<string, FileUploadItem>();

  newUploadFileTreeManager: DatasetVersionFileTreeManager = new DatasetVersionFileTreeManager();

  public fileDropped(files: NgxFileDropEntry[]) {
    for (const droppedFile of files) {
      if (droppedFile.fileEntry.isFile) {
        const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
        fileEntry.file(file => {
          const fileUploadItem = UserFileUploadService.createFileUploadItemWithPath(file, droppedFile.relativePath);
          this.newUploadFiles.set(fileUploadItem.name, fileUploadItem);
          this.newUploadFileTreeManager.createNodeWithPath(fileUploadItem.name);
          this.uploadedFiles.emit(Array.from(this.newUploadFiles.values()));
        });
      } else {
        // It was a directory (empty directories are added, otherwise only files)
        const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
        // TODO: add a prompt to notify user
        console.log(droppedFile.relativePath, fileEntry);
      }
    }
  }

  onPreviouslyUploadedFileDeleted(node: DatasetVersionFileTreeNode) {
    this.previouslyUploadFiles = this.previouslyUploadFiles?.filter(fileNode => fileNode != node);
    const paths = getPathsFromNode(node);
    this.removingFilePaths.emit(paths);
  }

  onNewUploadsFileDeleted(node: DatasetVersionFileTreeNode) {
    const filePath = getFullPathFromFileTreeNode(node);
    this.newUploadFileTreeManager.removeNodeWithPath(filePath);
    this.newUploadFiles.delete(filePath);
    this.uploadedFiles.emit(Array.from(this.newUploadFiles.values()));
  }
}
