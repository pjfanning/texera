export interface DatasetVersion {
  name: string;
  creationTime: number | undefined;
  versionHierarchyRoot: DatasetVersionHierarchyNode | undefined;
}

export interface DatasetVersionHierarchyNode {
  name: string;
  type: 'file' | 'directory';
  children?: DatasetVersionHierarchyNode[]; // Only populated if 'type' is 'directory'
}
