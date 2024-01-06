import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
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
  previouslyUploadFiles: DatasetVersionFileTreeNode[] | undefined;
  previouslyUploadFilesManager: DatasetVersionFileTreeManager | undefined;

  @Output()
  uploadedFiles = new EventEmitter<FileUploadItem[]>();

  @Output()
  removingFilePaths = new EventEmitter<string[]>();

  newUploadNodeToFileItems: Map<DatasetVersionFileTreeNode, FileUploadItem> = new Map<DatasetVersionFileTreeNode, FileUploadItem>();

  newUploadFileTreeManager: DatasetVersionFileTreeManager = new DatasetVersionFileTreeManager();
  newUploadFileTreeNodes: DatasetVersionFileTreeNode[] = [];


  public fileDropped(files: NgxFileDropEntry[]) {
    for (const droppedFile of files) {
      if (droppedFile.fileEntry.isFile) {
        const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
        fileEntry.file(file => {
          const fileUploadItem = UserFileUploadService.createFileUploadItemWithPath(file, droppedFile.relativePath);
          this.addFileToNewUploadsFileTree(droppedFile.relativePath, fileUploadItem);
          this.uploadedFiles.emit(Array.from(this.newUploadNodeToFileItems.values()));
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
    this.removeFileTreeNode(node, true);
    const paths = getPathsFromNode(node);
    this.removingFilePaths.emit(paths);
  }

  onNewUploadsFileDeleted(node: DatasetVersionFileTreeNode) {
    this.removeFileTreeNode(node, false);
    this.uploadedFiles.emit(Array.from(this.newUploadNodeToFileItems.values()));
  }

  private removeFileTreeNode(node: DatasetVersionFileTreeNode, fromPreviouslyUploads: boolean) {
    if (fromPreviouslyUploads) {
      if (!this.previouslyUploadFilesManager) {
        this.previouslyUploadFilesManager = new DatasetVersionFileTreeManager(this.previouslyUploadFiles);
      }
      if (this.previouslyUploadFilesManager) {
        this.previouslyUploadFilesManager.removeNode(node);
        console.log("before delete previous uploads", this.previouslyUploadFilesManager.getRootNodes())
        this.previouslyUploadFiles = [...this.previouslyUploadFilesManager.getRootNodes()]
        console.log("after delete previous uploads", this.previouslyUploadFiles)
      }
    } else {
      // from new uploads
      this.newUploadFileTreeManager.removeNode(node);
      this.newUploadFileTreeNodes = [...this.newUploadFileTreeManager.getRootNodes()];
      this.removeNodeAndChildrenFromFileItemsMap(node);
    }
  }

  private removeNodeAndChildrenFromFileItemsMap(node: DatasetVersionFileTreeNode) {
    this.newUploadNodeToFileItems.delete(node);

    // Recursively remove children if it's a directory
    if (node.type === "directory" && node.children) {
      node.children.forEach(child => this.removeNodeAndChildrenFromFileItemsMap(child));
    }
  }

  private addFileToNewUploadsFileTree(path: string, fileUploadItem: FileUploadItem) {
    const newNode = this.newUploadFileTreeManager.addNodeWithPath(path);
    this.newUploadFileTreeNodes = [...this.newUploadFileTreeManager.getRootNodes()];
    this.newUploadNodeToFileItems.set(newNode, fileUploadItem);
  }
}
