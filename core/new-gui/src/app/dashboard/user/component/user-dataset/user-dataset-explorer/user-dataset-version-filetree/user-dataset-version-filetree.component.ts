import { UntilDestroy } from "@ngneat/until-destroy";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { ITreeOptions, TREE_ACTIONS } from "@circlon/angular-tree-component";
import {DatasetVersionFileTreeNode, getFullPathFromFileTreeNode} from "../../../../../../common/type/datasetVersionFileTree";
import {NotificationService} from "../../../../../../common/service/notification/notification.service";

@UntilDestroy()
@Component({
  selector: "texera-user-dataset-version-filetree",
  templateUrl: "./user-dataset-version-filetree.component.html",
  styleUrls: ["./user-dataset-version-filetree.component.scss"],
})
export class UserDatasetVersionFiletreeComponent implements OnInit {
  @Input()
  public fileTreeNodeList: DatasetVersionFileTreeNode[] = [];

  @Input()
  public isFileTreeNodeDeletable: boolean = false;

  @Input()
  public isFileTreeNodePathCopyable: boolean = false;

  @Input()
  public did: number | undefined;

  @Input()
  public datasetName: string | undefined;

  @Output()
  public selectedTreeNode = new EventEmitter<DatasetVersionFileTreeNode>();

  @Output()
  public deletedTreeNode = new EventEmitter<DatasetVersionFileTreeNode>();

  constructor(private notificationService: NotificationService) {
  }

  public fileTreeDisplayOptions: ITreeOptions = {
    displayField: "displayableName",
    hasChildrenField: "children",
    actionMapping: {
      mouse: {
        click: (tree: any, node: any, $event: any) => {
          if (node.hasChildren) {
            TREE_ACTIONS.TOGGLE_EXPANDED(tree, node, $event);
          } else {
            this.selectedTreeNode.emit(node.data);
          }
        },
      },
    },
  };

  ngOnInit(): void {}

  deleteFileTreeNode(node: DatasetVersionFileTreeNode, $event: MouseEvent) {
    this.deletedTreeNode.emit(node);
    $event.stopPropagation();
  }

  copyFileTreeNodePath(node: DatasetVersionFileTreeNode, $event: MouseEvent) {
    if (this.isFileTreeNodePathCopyable && this.did && this.datasetName) {
      $event.stopPropagation();
      const filePath = `/${this.datasetName}-${this.did}${getFullPathFromFileTreeNode(node)}`;

      // Copying filePath to the clipboard
      navigator.clipboard.writeText(filePath).then(() => {
        // Notification on successful copying
        this.notificationService.success(`File path copied to the clipboard`);
      }).catch(err => {
        // Notification in case of an error during copying
        console.error('Failed to copy the file path to the clipboard', err);
        this.notificationService.error(`Failed to copy the file path`);
      });
    }
  }
}
