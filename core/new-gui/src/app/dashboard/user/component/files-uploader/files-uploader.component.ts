import {Component, EventEmitter, Output} from '@angular/core';
import {FileUploader} from 'ng2-file-upload';
import {UserFileUploadService} from '../../service/user-file/user-file-upload.service';
import {FileUploadItem} from "../../type/dashboard-file.interface";

@Component({
    selector: 'texera-user-files-uploader',
    templateUrl: './files-uploader.component.html',
    styleUrls: ['./files-uploader.component.scss']
})
export class FilesUploaderComponent {
    public uploader: FileUploader = new FileUploader({url: ""});
    private filesToBeUploaded: FileUploadItem[] = [];
    haveDropZoneOver: boolean = false;

    @Output()
    filesChanged = new EventEmitter<FileUploadItem[]>();

    constructor() {
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
        this.filesChanged.emit(this.filesToBeUploaded);
    }

    public handleClickUploadFile(clickUploadEvent: Event): void {
        const fileList: FileList | null = (clickUploadEvent.target as HTMLInputElement).files;
        if (fileList) {
            for (let i = 0; i < fileList.length; i++) {
                this.filesToBeUploaded.push(UserFileUploadService.createFileUploadItem(fileList[i]));
            }
            this.filesChanged.emit(this.filesToBeUploaded);
        }
    }

    public deleteNewFile(removedFile: FileUploadItem): void {
        this.filesToBeUploaded = this.filesToBeUploaded.filter(file => file !== removedFile);
        this.filesChanged.emit(this.filesToBeUploaded);
    }

    public getFileArray(): FileUploadItem[] {
        return this.filesToBeUploaded;
    }

    public getFileArrayLength(): number {
        return this.filesToBeUploaded.length;
    }
}
