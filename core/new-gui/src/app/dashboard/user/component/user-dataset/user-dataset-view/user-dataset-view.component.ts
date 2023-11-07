import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { NgbdModelDatasetFileAddComponent } from "./ngbd-model-dataset-file-add/ngbd-model-dataset-file-add.component";

@UntilDestroy()
@Component({
  templateUrl: "./user-dataset-view.component.html",
  styleUrls: ['./user-dataset-view.component.scss']
})
export class userDatasetViewComponent implements OnInit {
    did: number = 0;
    dName: string = "";
    isSiderCollapsed = false;
    versionNames: ReadonlyArray<string> = [];
    currentFile: string = "";

    //dummy
    files = [
        { name: 'file1', size: 'small' },
        { name: 'file2', size: 'medium' },
        { name: 'file3', size: 'large' },
        { name: 'file4', size: 'large' },
        { name: 'file5', size: 'large' },
      ];

    constructor(private route: ActivatedRoute, private datasetService: DatasetService, private modalService: NgbModal) {}

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.did = params['did'];
            this.dName = params['dname'];
        });

        this.datasetService
        .retrieveDatasetVersionList(this.did)
        .pipe(untilDestroyed(this))
        .subscribe( versionNames => {this.versionNames = versionNames; 
          console.log(versionNames)} )
    }

    loadContent(file: string) {
      this.currentFile = file;
    }

    clickToHideTree() {
      this.isSiderCollapsed = true;
    }
    
    clickToShowTree() {
      this.isSiderCollapsed = false;
    }

    public openFileAddComponent() {
      const modalRef = this.modalService.open(NgbdModelDatasetFileAddComponent);
  
      modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {

      });
      
      modalRef.componentInstance.did = this.did;
    }
}