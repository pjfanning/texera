import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FileUploadItem} from "../../type/dashboard-file.interface";
import {NgxFileDropEntry} from "ngx-file-drop";
import {DatasetVersionFileTreeNode} from "../../../../common/type/datasetVersion";
import {UserFileUploadService} from "../../service/user-file/user-file-upload.service";

@Component({
    selector: 'texera-user-files-uploader',
    templateUrl: './files-uploader.component.html',
    styleUrls: ['./files-uploader.component.scss']
})
export class FilesUploaderComponent {
    @Input()
    public previouslyUploadFiles: DatasetVersionFileTreeNode[] = [];

    @Output()
    uploadedFiles = new EventEmitter<FileUploadItem[]>();

    @Output()
    removingFilePaths = new EventEmitter<string[]>();

    newUploadFiles: FileUploadItem[] = [];

    public fileDropped(files: NgxFileDropEntry[]) {
        for (const droppedFile of files) {
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                fileEntry.file(file => {
                    console.log(droppedFile.relativePath + " upload successfully")
                    const fileUploadItem = UserFileUploadService.createFileUploadItemWithPath(file, droppedFile.relativePath)
                    this.newUploadFiles.push(fileUploadItem);
                    this.uploadedFiles.emit(this.newUploadFiles)
                })
            } else {
                // It was a directory (empty directories are added, otherwise only files)
                const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
                console.log(droppedFile.relativePath, fileEntry);
                console.log("empty directory found")
            }
        }
    }

    public fileOver(event: any){
        console.log(event);
    }

    public fileLeave(event: any){
        console.log(event);
    }

    public getNumberOfPreviouslyUploadFiles(): number {
        // TODO: finish this
        return 0;
    }

    public getNumberOfNewUploadFiles(): number {
        return this.newUploadFiles.length
    }

    public getNewUploadFileEntries(): FileUploadItem[] {
        return this.newUploadFiles
    }

    public removeNewUploadFile(file: FileUploadItem) {
        this.newUploadFiles = this.newUploadFiles.filter(f => f != file)
        this.uploadedFiles.emit(this.newUploadFiles)
    }
}
