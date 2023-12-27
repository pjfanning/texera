import {Component, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {DatasetService} from "../../../service/user-dataset/dataset.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgbdModelCreateNewVersion} from "../ngbd-model-dataset-version-add/ngbd-model-create-new-version.component";
import {DatasetVersion, DatasetVersionHierarchyNode} from "src/app/common/type/datasetVersion";
import {TREE_ACTIONS, KEYS, IActionMapping, ITreeOptions, TreeModel, TreeNode} from '@circlon/angular-tree-component';
import {FileSizeLimits} from "src/app/common/type/datasetVersion";

// import * as Papa from 'papaparse';


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

  public isSiderCollapsed = false;
  public isMaximized = false;

  public versions: ReadonlyArray<DatasetVersion> = [];
  public selectedVersion: DatasetVersion | undefined;
  public dataNodeList: DatasetVersionHierarchyNode[] = [];

  constructor(
    private route: ActivatedRoute,
    private datasetService: DatasetService,
    private modalService: NgbModal
  ) {
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
        this.selectedVersion = this.versions[0];
        this.onVersionSelected(this.selectedVersion);
      });
  }

  loadFileContent(file: string, prefix: string) {
    let path = file;
    if (prefix !== "") {
      path = prefix + "/" + file;
    }
    this.currentDisplayedFileName = path;
  }

  clickToMaximizeMinimize() {
    this.isMaximized = !this.isMaximized;
  }

  clickToHideShowTree() {
    this.isSiderCollapsed = !this.isSiderCollapsed;
  }

  onVersionSelected(version: DatasetVersion): void {
    this.selectedVersion = version;

    if (this.selectedVersion.dvid)
      this.datasetService
        .retrieveDatasetVersionFileHierarchy(this.did, this.selectedVersion.dvid)
        .pipe(untilDestroyed(this))
        .subscribe(dataNodeList => {
          this.dataNodeList = dataNodeList;
          let currentNode = this.dataNodeList[0];
          while (currentNode.type === "directory" && currentNode.children) {
            currentNode = currentNode.children[0];
          }
          console.log(currentNode)
          this.loadFileContent(currentNode.name, currentNode.dir);
        })
  }

  public onClickOpenVersionCreationWindow() {
    const modalRef = this.modalService.open(NgbdModelCreateNewVersion);
    modalRef.componentInstance.did = this.did;

    modalRef.componentInstance.versionAdded.pipe(untilDestroyed(this))
      .subscribe(() => {
        this.refreshVersionList();
      });

    // set the base version for the modalRef
    modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {
    });
  }


  options: ITreeOptions = {
    displayField: 'name',
    hasChildrenField: 'children',
    actionMapping: {
      mouse: {
        click: (tree: any, node: any, $event: any) => {
          if (node.hasChildren) {
            TREE_ACTIONS.TOGGLE_EXPANDED(tree, node, $event);
          } else {
            this.loadFileContent(node.data.name, node.data.dir);
          }
        }
      }
    }
  };
}
