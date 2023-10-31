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
    dataset_name: String = "";

    //dummy
    files = [
        { name: 'file1', size: 'small' },
        { name: 'file2', size: 'medium' },
        { name: 'file4', size: 'large' },
      ];

    constructor(private route: ActivatedRoute, private datasetService: DatasetService) {}

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.did = params['dataset_id'];
        });
    }

    loadContent(file: string) {
        console.log(`Clicked on ${file}`);
      }
}