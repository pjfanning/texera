import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DatasetService } from "../../../service/user-dataset/dataset.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { NgbdModelDatasetFileAddComponent } from "./ngbd-model-dataset-file-add/ngbd-model-dataset-file-add.component";
import { DatasetVersionHierarchyNode } from "src/app/common/type/datasetVersion";
import * as Papa from 'papaparse';
import { TREE_ACTIONS, KEYS, IActionMapping, ITreeOptions, TreeModel, TreeNode } from '@circlon/angular-tree-component';


@UntilDestroy()
@Component({
  templateUrl: "./user-dataset-view.component.html",
  styleUrls: ['./user-dataset-view.component.scss']
})
export class userDatasetViewComponent implements OnInit {

    public did: number = 0;
    public dName: string = "";
    public dDescription: string = "";
    public createdTime: string = "";

    public currentFile: string = "";
    public currentFileObject: File | undefined = undefined;
    public fileURL: string = "";
    public blobFile: Blob = new Blob();
    public csvContent: any[] = [];
    public pdfDisplay: boolean = false;
    public csvDisplay: boolean = false;

    public isSiderCollapsed = false;
    public isMaximized = false;

    public versionNames: ReadonlyArray<string> = [];
    public selectedVersion: string = "";
    public dataNodeList: DatasetVersionHierarchyNode[] = [];

    constructor(
      private route: ActivatedRoute,
      private datasetService: DatasetService,
      private modalService: NgbModal
    ) {}

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.did = params['did'];
        });

        this.datasetService
        .getDataset(this.did)
        .pipe(untilDestroyed(this))
        .subscribe(dataset => {
          this.dName = dataset.name;
          this.dDescription = dataset.description;
          if (typeof dataset.creationTime === 'number') {
            this.createdTime = new Date(dataset.creationTime).toString();
          }
        });

        this.datasetService
        .retrieveDatasetVersionList(this.did)
        .pipe(untilDestroyed(this))
        .subscribe( versionNames => {
          this.versionNames = versionNames;
          this.selectedVersion = this.versionNames[0];
          this.onVersionSelected(this.selectedVersion);
        });

    }

    turnOffAllDisplay() {
      this.pdfDisplay = false;
      this.csvDisplay = false;
    }

    loadContent(file: string, prefix: string) {
      this.currentFile = file;
      let path = file;

      this.turnOffAllDisplay();

      if (prefix !== "") {
        path = prefix + "/" + file;
      }
      
      this.datasetService
      .inspectDatasetSingleFile(this.did, this.selectedVersion, path)
      .pipe(untilDestroyed(this))
      .subscribe(blob => {
        this.blobFile = blob;
        this.currentFileObject = new File([blob], this.currentFile, { type: blob.type });
        this.fileURL = URL.createObjectURL(blob);
        if (this.currentFile.endsWith(".pdf")) {
          setTimeout(() => this.pdfDisplay = true, 0);
        } else if (this.currentFile.endsWith(".csv")) {
          Papa.parse(this.currentFileObject, {
            complete: (results) => {
              this.csvContent = results.data;
            }
          });
          this.csvDisplay = true;
        } else {
          this.turnOffAllDisplay();
        }
      })
    }

    clickToMaximizeMinimize() {
      this.isMaximized = !this.isMaximized;
    }

    clickToHideShowTree() {
      this.isSiderCollapsed = !this.isSiderCollapsed;
    }

    onVersionSelected(versionName: string): void {
      this.selectedVersion = versionName;

      this.datasetService
      .retrieveDatasetVersionFileHierarchy(this.did, this.selectedVersion)
      .pipe(untilDestroyed(this))
      .subscribe(dataNodeList => {
        this.dataNodeList = dataNodeList;
        let currentNode = this.dataNodeList[0];
        while (currentNode.type === "directory" && currentNode.children) {
          currentNode = currentNode.children[0];
        }
        this.loadContent(currentNode.name, currentNode.dir);
      })
    }

    public openFileAddComponent() {
      const modalRef = this.modalService.open(NgbdModelDatasetFileAddComponent);

      modalRef.dismissed.pipe(untilDestroyed(this)).subscribe(_ => {

      });

      modalRef.componentInstance.did = this.did;
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
              this.loadContent(node.data.name, node.data.dir);
            }
          }
        }
      }
    };
 
}
