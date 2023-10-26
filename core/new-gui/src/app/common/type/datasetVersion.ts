export interface DatasetVersion {
  name: string;
  creationTime: number | undefined;
  versionHierarchyRoot: DatasetVersionHierarchyNode[] | undefined;
}

export interface DatasetVersionHierarchy {
  [key: string] : DatasetVersionHierarchy | string
}

export interface DatasetVersionHierarchyNode {
  name: string;
  type: 'file' | 'directory';
  children?: DatasetVersionHierarchyNode[]; // Only populated if 'type' is 'directory'
}

export function parseHierarchyToNodes(hierarchy: DatasetVersionHierarchy): DatasetVersionHierarchyNode[] {
  const isDirectory = (node: DatasetVersionHierarchy | string): node is DatasetVersionHierarchy => {
    return typeof node === 'object' && node !== null && !(node instanceof Array);
  };

  const parseHierarchyToNode = (h: DatasetVersionHierarchy | string, nodeName: string): DatasetVersionHierarchyNode => {
    if (isDirectory(h)) {
      return {
        name: nodeName,
        type: "directory",
        children: Object.keys(h).map(key => parseHierarchyToNode(h[key], key))
      };
    } else {
      return {
        name: nodeName,
        type: "file"
      };
    }
  };

  if (!isDirectory(hierarchy)) {
    throw new Error('The provided hierarchy is not a valid directory structure.');
  }

  return Object.keys(hierarchy).map(key => parseHierarchyToNode(hierarchy[key], key));
}

