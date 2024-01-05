import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { NgbdModelDatasetVersionAddComponent } from "../ngbd-model-dataset-version-add/ngbd-model-dataset-version-add.component";
import { TREE_ACTIONS, KEYS, IActionMapping, ITreeOptions, TreeModel, TreeNode } from "@circlon/angular-tree-component";
import { NzResizeEvent } from "ng-zorro-antd/resizable";
import {
  DatasetVersionFileTreeNode,
  getFullPathFromFileTreeNode,
} from "../../../../../common/type/datasetVersionFileTree";
import { DatasetVersion } from "../../../../../common/type/dataset";

@UntilDestroy()
@Component({
  templateUrl: "./user-dataset-explorer.component.html",
  styleUrls: ["./user-dataset-explorer.component.scss"],
})
export class UserDatasetExplorerComponent implements OnInit {
  public did: number | undefined;
  public datasetName: string = "";
  public datasetDescription: string = "";
  public datasetCreationTime: string = "";

  public currentDisplayedFileName: string = "";

  public isRightBarCollapsed = false;
  public isMaximized = false;

  public versions: ReadonlyArray<DatasetVersion> = [];
  public selectedVersion: DatasetVersion | undefined;
  public fileTreeNodeList: DatasetVersionFileTreeNode[] = [];

  public isCreatingVersion: boolean = false;
  public isCreatingDataset: boolean = false;
  public versionCreatorBaseVersion: DatasetVersion | undefined;

  constructor(private route: ActivatedRoute, private router: Router, private datasetService: DatasetService) {}

  // item for control the resizeable sider
  MAX_SIDER_WIDTH = 400;
  MIN_SIDER_WIDTH = 150;
  siderWidth = 200;
  id = -1;
  onSideResize({ width }: NzResizeEvent): void {
    cancelAnimationFrame(this.id);
    this.id = requestAnimationFrame(() => {
      this.siderWidth = width!;
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      console.log("enter init");

      const param = params["did"];
      if (param != "create") {
        this.did = param;
        this.retrieveDatasetInfo();
        this.retrieveDatasetVersionList();
      } else {
        this.renderDatasetCreatorSider();
      }
    });
  }

  renderDatasetCreatorSider() {
    this.isCreatingVersion = false;
    this.isCreatingDataset = true;
    this.siderWidth = this.MAX_SIDER_WIDTH;
  }

  renderVersionCreatorSider() {
    this.isCreatingDataset = false;
    this.isCreatingVersion = true;
    this.siderWidth = this.MAX_SIDER_WIDTH;
  }

  public onCreationFinished(creationID: number) {
    if (creationID != 0) {
      // creation succeed
      if (this.isCreatingVersion) {
        this.retrieveDatasetVersionList();
      } else {
        this.router.navigate([`/dashboard/dataset/${creationID}`]);
      }
    } else {
      // creation failed
      if (this.isCreatingVersion) {
        this.isCreatingVersion = false;
        this.isCreatingDataset = false;
        this.retrieveDatasetVersionList();
      } else {
        this.router.navigate(["/dashboard/dataset"]);
      }
    }
  }

  public onClickOpenVersionCreator() {
    this.renderVersionCreatorSider();
  }

  retrieveDatasetInfo() {
    if (this.did) {
      this.datasetService
        .getDataset(this.did)
        .pipe(untilDestroyed(this))
        .subscribe(dataset => {
          this.datasetName = dataset.name;
          this.datasetDescription = dataset.description;
          if (typeof dataset.creationTime === "number") {
            this.datasetCreationTime = new Date(dataset.creationTime).toString();
          }
        });
    }
  }

  retrieveDatasetVersionList() {
    if (this.did) {
      this.datasetService
        .retrieveDatasetVersionList(this.did)
        .pipe(untilDestroyed(this))
        .subscribe(versionNames => {
          this.versions = versionNames;
          // by default, the selected version is the 1st element in the retrieved list
          // which is guaranteed(by the backend) to be the latest created version.
          this.selectedVersion = this.versions[0];
          this.onVersionSelected(this.selectedVersion);
        });
    }
  }

  loadFileContent(node: DatasetVersionFileTreeNode) {
    this.currentDisplayedFileName = getFullPathFromFileTreeNode(node);
  }

  onClickScaleTheView() {
    this.isMaximized = !this.isMaximized;
  }

  onClickHideRightBar() {
    this.isRightBarCollapsed = !this.isRightBarCollapsed;
  }

  onVersionSelected(version: DatasetVersion): void {
    this.selectedVersion = version;
    if (this.did && this.selectedVersion.dvid)
      this.datasetService
        .retrieveDatasetVersionFileTree(this.did, this.selectedVersion.dvid)
        .pipe(untilDestroyed(this))
        .subscribe(dataNodeList => {
          this.fileTreeNodeList = dataNodeList;
          console.log(this.fileTreeNodeList);
          let currentNode = this.fileTreeNodeList[0];
          while (currentNode.type === "directory" && currentNode.children) {
            currentNode = currentNode.children[0];
          }
          this.loadFileContent(currentNode);
        });
  }

  onVersionFileTreeNodeSelected(node: DatasetVersionFileTreeNode) {
    this.loadFileContent(node);
  }

  isDisplayingDataset(): boolean {
    return !this.isCreatingDataset && !this.isCreatingVersion;
  }
}
