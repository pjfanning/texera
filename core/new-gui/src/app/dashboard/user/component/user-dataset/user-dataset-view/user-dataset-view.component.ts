import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../service/user-dataset/dataset.service";

@UntilDestroy()
@Component({
  templateUrl: "./user-dataset-view.component.html",
})
export class userDatasetComponent implements OnInit {
    did: number = 0;
    dataset_name: String = "";

    constructor(private route: ActivatedRoute, private datasetService: DatasetService) {}

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.did = params['dataset_id'];
        });
    }
}