import {UntilDestroy} from "@ngneat/until-destroy";
import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {DatasetVersionFileTreeNode} from "../../../../../../common/type/datasetVersion";
import {ITreeOptions, TREE_ACTIONS} from "@circlon/angular-tree-component";

@UntilDestroy()
@Component({
    selector: "texera-user-dataset-version-filetree",
    templateUrl: "./user-dataset-version-filetree.component.html",
    styleUrls: ["./user-dataset-version-filetree.component.scss"]
})
export class UserDatasetVersionFiletreeComponent implements OnInit {
    @Input()
    public fileTreeNodeList: DatasetVersionFileTreeNode[] = [];

    @Input()
    public isFileTreeNodeDeletable: boolean = false;

    @Output()
    public selectedTreeNode = new EventEmitter<DatasetVersionFileTreeNode>();

    @Output()
    public deletedTreeNode = new EventEmitter<DatasetVersionFileTreeNode>();

    public fileTreeDisplayOptions: ITreeOptions = {
        displayField: 'displayableName',
        hasChildrenField: 'children',
        actionMapping: {
            mouse: {
                click: (tree: any, node: any, $event: any) => {
                    if (node.hasChildren) {
                        TREE_ACTIONS.TOGGLE_EXPANDED(tree, node, $event);
                    } else {
                        this.selectedTreeNode.emit(node.data)
                    }
                }
            }
        }
    };

    ngOnInit(): void {
    }

    deleteFileTreeNode(node: DatasetVersionFileTreeNode) {
        this.deletedTreeNode.emit(node)
    }
}
