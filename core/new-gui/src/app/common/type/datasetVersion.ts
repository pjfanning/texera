export interface DatasetVersion {
  dvid: number | undefined;
  did: number;
  name: string;
  versionHash: string | undefined;
  creationTime: number | undefined;
  versionFileTreeNodes: DatasetVersionFileTreeNode[] | undefined;
}

export interface DatasetVersionFileTree {
  [key: string] : DatasetVersionFileTree | string
}

export interface DatasetVersionFileTreeNode {
  name: string;
  type: 'file' | 'directory';
  children?: DatasetVersionFileTreeNode[]; // Only populated if 'type' is 'directory'
  dir: string;
}

export interface FileSizeLimits {
 [key: string]: number;
}


export function parseFileTreeToNodes(hierarchy: DatasetVersionFileTree): DatasetVersionFileTreeNode[] {
  const isDirectory = (node: DatasetVersionFileTree | string): node is DatasetVersionFileTree => {
    return typeof node === 'object' && node !== null && !(node instanceof Array);
  };

  const parseHierarchyToNode = (h: DatasetVersionFileTree | string, nodeName: string, dir: string): DatasetVersionFileTreeNode => {
    if (isDirectory(h)) {
      let path = dir + "/" + nodeName;
      return {
        name: nodeName,
        type: "directory",
        children: Object.keys(h).map(key => parseHierarchyToNode(h[key], key, path)),
        dir: ""
      };
    } else {
      return {
        name: nodeName,
        type: "file",
        dir: dir
      };
    }
  };

  if (!isDirectory(hierarchy)) {
    throw new Error('The provided hierarchy is not a valid directory structure.');
  }

  return Object.keys(hierarchy).map(key => parseHierarchyToNode(hierarchy[key], key, ""));
}

