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
  displayableName: string;
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
        parentDir: currentDir,
        displayableName: convertFileNameToDisplayableName(nodeName)
      };
    } else {
      return {
        name: nodeName,
        type: "file",
        parentDir: currentDir,
        displayableName: convertFileNameToDisplayableName(nodeName)
      };
    }
  };

  if (!isDirectory(hierarchy)) {
    throw new Error('The provided hierarchy is not a valid directory structure.');
  }

  return Object.keys(hierarchy).map(key => parseHierarchyToNode(hierarchy[key], key, parentDir));
}


function convertFileNameToDisplayableName(name: string): string {
  const nameLimit = 10;
  // Check if the name is already 10 characters or less
  if (name.length <= nameLimit) {
    return name;
  }

  // Split the name into base and extension
  const lastDotIndex = name.lastIndexOf('.');
  const base = name.substring(0, lastDotIndex);
  const extension = lastDotIndex !== -1 ? name.substring(lastDotIndex) : '';

  // Shorten the base part if necessary
  if (base.length > nameLimit) {
    // If there is an extension, leave space for it and '...'
    const shortenedBase = extension ? base.substring(0, nameLimit-3) : base.substring(0, nameLimit);
    return `${shortenedBase}...${extension}`;
  } else {
    // If the base is within limits, but the whole name isn't
    return `${base.substring(0, nameLimit-3)}...${extension}`;
  }
}


