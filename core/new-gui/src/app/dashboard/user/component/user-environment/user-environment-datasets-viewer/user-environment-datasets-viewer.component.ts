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
export class UserEnvironmentDatasetsViewerComponent implements OnInit, OnChanges {

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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.datasetIdToVersionId) {
            // Check if the value has changed by comparing the current value to the previous value
            const prev = changes.datasetIdToVersionId.previousValue as Map<DashboardDataset, number>;
            const curr = changes.datasetIdToVersionId.currentValue as Map<DashboardDataset, number>;
            //
            // this.handleMapChange(prev, curr);
        }
    }

    // private handleMapChange(prev: Map<DashboardDataset, number>, curr: Map<DashboardDataset, number>): void {
    //     // New key-value pairs added
    //     curr.forEach((currValue, currKey) => {
    //         if (!prev.has(currKey)) {
    //             if (currKey.dataset.did) {
    //                 const did = currKey.dataset.did;
    //                 this.updateNewDatasetWithVersion(did, currValue, currKey.dataset.name);
    //             }
    //         }
    //     });
    //
    //     // Old keys that might have new values or be deleted
    //     prev.forEach((prevValue, prevKey) => {
    //         if (!curr.has(prevKey)) {
    //             // Handle deleted key
    //             if (prevKey.dataset.did)
    //                 this.datasetFileTreeNodeLists.delete([prevKey.dataset.did, prevValue]);
    //         } else if (curr.get(prevKey) !== prevValue) {
    //             // Handle old key with new value
    //             if (prevKey.dataset.did) {
    //                 this.updateNewDatasetWithVersion(prevKey.dataset.did, prevValue, prevKey.dataset.name);
    //             }
    //         }
    //     });
    // }


    ngOnInit(): void {
        this.datasetIdToDatasetOfEnvironmentDetails.forEach((value, key) => {
            const did = key[0];
            const dvid = key[1];
            const dataset = value.dataset;
            this.updateNewDatasetWithVersion(did, dvid, dataset.name);
        })
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
}
