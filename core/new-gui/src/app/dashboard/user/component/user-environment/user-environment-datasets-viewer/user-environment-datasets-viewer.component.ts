import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {index} from "d3";
import {EnvironmentService} from "../../../service/user-environment/environment.service";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {DatasetVersionFileTreeNode} from "../../../../../common/type/datasetVersionFileTree";
import {DashboardDataset} from "../../../type/dashboard-dataset.interface";
import {NotificationService} from "../../../../../common/service/notification/notification.service";
import {DatasetOfEnvironmentDetails} from "../../../type/environment";

@Component({
    selector: 'texera-user-environment-datasets-viewer',
    templateUrl: './user-environment-datasets-viewer.component.html',
    styleUrls: ['./user-environment-datasets-viewer.component.scss']
})
export class UserEnvironmentDatasetsViewerComponent implements OnInit, OnChanges{

    // from did to dvid
    @Input()
    public eid: number | undefined;

    // [did, dvid] => DatasetOfEnvironmentDetails
    @Input()
    public datasetIdToDatasetOfEnvironmentDetails: Map<[number, number], DatasetOfEnvironmentDetails> = new Map();

    // [did, dvid] to [datasetName, datasetVersionFileTreeNode[]]
    datasetFileTreeNodeLists: Map<[number, number], [string, DatasetVersionFileTreeNode[]]> = new Map();

    onMoreAction(index: number, event: MouseEvent): void {
        event.stopPropagation(); // Prevent the collapse panel from toggling
        console.log('More action clicked on panel index:', index);
        // Implement your more action logic here
    }

    constructor(
        private environmentService: EnvironmentService,
        private datasetService: DatasetService,
        private notificationService: NotificationService) {
    }

    ngOnInit(): void {
        this.loadFromDatasetOfEnvironmentDetails();
    }

    ngOnChanges(changes: SimpleChanges) {
        this.datasetFileTreeNodeLists.clear();
        this.loadFromDatasetOfEnvironmentDetails();
        console.log("changes!")
        // console.log(this.datasetFileTreeNodeLists);
        console.log(this.datasetIdToDatasetOfEnvironmentDetails);
    }

    loadFromDatasetOfEnvironmentDetails() {
        this.datasetIdToDatasetOfEnvironmentDetails.forEach((value, key) => {
            const did = key[0];
            const dvid = key[1];
            const dataset = value.dataset;
            this.updateNewDatasetWithVersion(did, dvid, dataset.name);
        });
    }

    private updateNewDatasetWithVersion(did: number, dvid: number, datasetName: string) {
        this.datasetService
            .retrieveDatasetVersionFileTree(did, dvid)
            .subscribe({
                next: nodes => {
                    this.datasetFileTreeNodeLists.set([did, dvid], [datasetName, nodes])
                },
                error: (error: unknown) => {
                    this.notificationService.error(`Error loading dataset info`);
                },
            })
    }

    get datasets(): [string, DatasetVersionFileTreeNode[]][]{
        return Array.from(this.datasetFileTreeNodeLists.values());
    }
}
