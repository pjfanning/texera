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
  parentDir: string;
}
export function parseFileTreeToNodes(hierarchy: DatasetVersionFileTree, parentDir: string = "/"): DatasetVersionFileTreeNode[] {
  const isDirectory = (node: DatasetVersionFileTree | string): node is DatasetVersionFileTree => {
    return typeof node === 'object' && node !== null && !(node instanceof Array);
  };

  const parseHierarchyToNode = (node: DatasetVersionFileTree | string, nodeName: string, currentDir: string): DatasetVersionFileTreeNode => {
    if (isDirectory(node)) {
      let dir = currentDir === "/" ? `/${nodeName}` : `${currentDir}/${nodeName}`;
      return {
        name: nodeName,
        type: "directory",
        children: Object.keys(node).map(key => parseHierarchyToNode(node[key], key, dir)),
        parentDir: currentDir
      };
    } else {
      return {
        name: nodeName,
        type: "file",
        parentDir: currentDir
      };
    }
  };

  if (!isDirectory(hierarchy)) {
    throw new Error('The provided hierarchy is not a valid directory structure.');
  }

  return Object.keys(hierarchy).map(key => parseHierarchyToNode(hierarchy[key], key, parentDir));
}


