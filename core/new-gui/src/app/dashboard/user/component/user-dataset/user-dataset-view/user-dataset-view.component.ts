import {Component, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModelDatasetVersionAddComponent} from "../ngbd-model-dataset-version-add/ngbd-model-dataset-version-add.component";
import {TREE_ACTIONS, KEYS, IActionMapping, ITreeOptions, TreeModel, TreeNode} from '@circlon/angular-tree-component';
import {NzResizeEvent} from "ng-zorro-antd/resizable";
import {DatasetVersion, DatasetVersionFileTreeNode, getFullPathFromFileTreeNode} from "../../../../../common/type/datasetVersionFileTree";

@UntilDestroy()
@Component({
    templateUrl: "./user-dataset-view.component.html",
    styleUrls: ['./user-dataset-view.component.scss']
})
export class UserDatasetViewComponent implements OnInit {

    public did: number = 0;
    public datasetName: string = "";
    public datasetDescription: string = "";
    public datasetCreationTime: string = "";

    public currentDisplayedFileName: string = "";

    public isRightBarCollapsed = false;
    public isMaximized = false;

    public versions: ReadonlyArray<DatasetVersion> = [];
    public selectedVersion: DatasetVersion | undefined;
    public fileTreeNodeList: DatasetVersionFileTreeNode[] = [];

    constructor(
        private route: ActivatedRoute,
        private datasetService: DatasetService,
        private modalService: NgbModal
    ) {
    }

    // item for control the resizeable sider
    siderWidth = 200;
    id = -1;
    onSideResize({width}: NzResizeEvent): void {
        console.log("sider: ", width)

        cancelAnimationFrame(this.id);
        this.id = requestAnimationFrame(() => {
            this.siderWidth = width!;
        });
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.did = params['did'];
        });

        this.datasetService
            .getDataset(this.did)
            .pipe(untilDestroyed(this))
            .subscribe(dataset => {
                this.datasetName = dataset.name;
                this.datasetDescription = dataset.description;
                if (typeof dataset.creationTime === 'number') {
                    this.datasetCreationTime = new Date(dataset.creationTime).toString();
                }
            });

        this.datasetService
            .retrieveDatasetVersionList(this.did)
            .pipe(untilDestroyed(this))
            .subscribe(versions => {
                this.versions = versions;
                this.selectedVersion = this.versions[0];
                this.onVersionSelected(this.selectedVersion);
            });

    }

    refreshVersionList() {
        this.datasetService.retrieveDatasetVersionList(this.did)
            .pipe(untilDestroyed(this))
            .subscribe(versionNames => {
                this.versions = versionNames;
                // by default, the selected version is the 1st element in the retrieved list
                // which is guaranteed(by the backend) to be the latest created version.
                this.selectedVersion = this.versions[0];
                this.onVersionSelected(this.selectedVersion);
            });
    }

    loadFileContent(node: DatasetVersionFileTreeNode) {
        this.currentDisplayedFileName = getFullPathFromFileTreeNode(node)
    }

    onClickScaleTheView() {
        this.isMaximized = !this.isMaximized;
    }

    onClickHideRightBar() {
        this.isRightBarCollapsed = !this.isRightBarCollapsed;
    }

    onVersionSelected(version: DatasetVersion): void {
        this.selectedVersion = version;
        console.log(this.selectedVersion.dvid)
        if (this.selectedVersion.dvid)
            this.datasetService
                .retrieveDatasetVersionFileTree(this.did, this.selectedVersion.dvid)
                .pipe(untilDestroyed(this))
                .subscribe(dataNodeList => {
                    this.fileTreeNodeList = dataNodeList;
                    console.log(this.fileTreeNodeList)
                    let currentNode = this.fileTreeNodeList[0];
                    while (currentNode.type === "directory" && currentNode.children) {
                        currentNode = currentNode.children[0];
                    }
                    this.loadFileContent(currentNode);
                })
    }

    onVersionFileTreeNodeSelected(node: DatasetVersionFileTreeNode) {
        this.loadFileContent(node)
    }

    public onClickOpenVersionCreationWindow() {
        const modalRef = this.modalService.open(NgbdModelDatasetVersionAddComponent);
        modalRef.componentInstance.did = this.did;

        modalRef.componentInstance.versionAdded.pipe(untilDestroyed(this))
            .subscribe(() => {
                this.refreshVersionList();
            });

        // set the base version for the modalRef
        modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {
        });
    }
}
