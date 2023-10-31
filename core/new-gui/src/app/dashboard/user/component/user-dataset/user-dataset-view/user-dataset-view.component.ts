import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../service/user-dataset/dataset.service";

@UntilDestroy()
@Component({
  templateUrl: "./user-dataset-view.component.html",
})
export class userDatasetViewComponent implements OnInit {
    did: number = 0;
    dName: string = "";
    versionNames: ReadonlyArray<string> = [];

    //dummy
    files = [
        { name: 'file1', size: 'small' },
        { name: 'file2', size: 'medium' },
        { name: 'file4', size: 'large' },
      ];

    constructor(private route: ActivatedRoute, private datasetService: DatasetService) {}

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
        console.log(`Clicked on ${file}`);
    }
}