import { FileUploadItem } from "../../dashboard/user/type/dashboard-file.interface";

export interface DatasetVersionFileTree {
  [key: string]: DatasetVersionFileTree | string;
}

export interface DatasetVersionFileTreeNode {
  name: string;
  type: "file" | "directory";
  children?: DatasetVersionFileTreeNode[]; // Only populated if 'type' is 'directory'
  parentDir: string;
}

export function getFullPathFromFileTreeNode(node: DatasetVersionFileTreeNode): string {
  if (node.parentDir == "/") {
    return node.parentDir + node.name;
  } else {
    return node.parentDir + "/" + node.name;
  }
}

export function getPathsFromNode(node: DatasetVersionFileTreeNode): string[] {
  // Helper function to recursively gather paths
  const gatherPaths = (node: DatasetVersionFileTreeNode, currentPath: string): string[] => {
    // Base case: if the node is a file, return its path
    if (node.type === "file") {
      return [currentPath];
    }

    // Recursive case: if the node is a directory, explore its children
    let paths = node.children ? node.children.flatMap(child => gatherPaths(child, currentPath + "/" + child.name)) : [];

    // Include the directory's own path if it's not the root
    if (node.name !== "/") {
      paths.unshift(currentPath);
    }

    return paths;
  };

  return gatherPaths(node, node.parentDir === "/" ? "/" + node.name : node.parentDir + "/" + node.name);
}

export class DatasetVersionFileTreeManager {
  private root: DatasetVersionFileTreeNode = { name: "/", type: "directory", children: [], parentDir: "" };
  private treeNodesMap: Map<string, DatasetVersionFileTreeNode> = new Map<string, DatasetVersionFileTreeNode>();

  constructor() {
    this.treeNodesMap.set("/", this.root);
  }

  private updateTreeMapWithPath(path: string): DatasetVersionFileTreeNode {
    const pathParts = path.startsWith("/") ? path.slice(1).split("/") : path.split("/");
    let currentPath = "/";
    let currentNode = this.root;

    pathParts.forEach((part, index) => {
      const previousPath = currentPath;
      currentPath += part + (index < pathParts.length - 1 ? "/" : ""); // Don't add trailing slash for last part

      if (!this.treeNodesMap.has(currentPath)) {
        const isLastPart = index === pathParts.length - 1;
        const newNode: DatasetVersionFileTreeNode = {
          name: part,
          type: isLastPart ? "file" : "directory",
          parentDir: previousPath.endsWith("/") ? previousPath.slice(0, -1) : previousPath, // Store the full path for parentDir
          ...(isLastPart ? {} : { children: [] }), // Only add 'children' for directories
        };
        this.treeNodesMap.set(currentPath, newNode);
        currentNode.children = currentNode.children ?? []; // Ensure 'children' is initialized
        currentNode.children.push(newNode);
      }
      currentNode = this.treeNodesMap.get(currentPath)!; // Get the node for the next iteration
    });

    return currentNode;
  }

  private removeNodeAndDescendants(node: DatasetVersionFileTreeNode): void {
    if (node.type === "directory" && node.children) {
      node.children.forEach(child => {
        const childPath =
          node.parentDir === "/" ? `/${node.name}/${child.name}` : `${node.parentDir}/${node.name}/${child.name}`;
        this.removeNodeAndDescendants(child);
        this.treeNodesMap.delete(childPath); // Remove the child from the map
      });
    }
    // Now that all children are removed, clear the current node's children array
    node.children = [];
  }

  createNodeWithPath(path: string): void {
    this.updateTreeMapWithPath(path);
  }

  removeNodeWithPath(path: string): void {
    const nodeToRemove = this.treeNodesMap.get(path);
    if (nodeToRemove) {
      // First, recursively remove all descendants of the node
      this.removeNodeAndDescendants(nodeToRemove);

      // Then, remove the node from its parent's children array
      const parentNode = this.treeNodesMap.get(nodeToRemove.parentDir);
      if (parentNode && parentNode.children) {
        parentNode.children = parentNode.children.filter(child => child.name !== nodeToRemove.name);
      }

      // Finally, remove the node from the map
      this.treeNodesMap.delete(path);
    }
  }

  getRootNodes(): DatasetVersionFileTreeNode[] {
    return this.root.children ?? [];
  }
}
export function parseFileUploadItemToVersionFileTreeNodes(
  fileUploadItems: FileUploadItem[]
): DatasetVersionFileTreeNode[] {
  const root: DatasetVersionFileTreeNode = { name: "/", type: "directory", children: [], parentDir: "" };
  const treeNodesMap = new Map<string, DatasetVersionFileTreeNode>();
  treeNodesMap.set("/", root);

  fileUploadItems.forEach(item => {
    const pathParts = item.name.startsWith("/") ? item.name.slice(1).split("/") : item.name.split("/");
    let currentPath = "/";
    let currentNode = root;

    pathParts.forEach((part, index) => {
      currentPath += part + (index < pathParts.length - 1 ? "/" : ""); // Don't add trailing slash for last part
      if (!treeNodesMap.has(currentPath)) {
        const isLastPart = index === pathParts.length - 1;
        const newNode: DatasetVersionFileTreeNode = {
          name: part,
          type: isLastPart ? "file" : "directory",
          parentDir: currentNode.name,
          ...(isLastPart ? {} : { children: [] }), // Only add 'children' for directories
        };
        treeNodesMap.set(currentPath, newNode);
        currentNode.children = currentNode.children ?? []; // Ensure 'children' is initialized
        currentNode.children.push(newNode);
      }
      currentNode = treeNodesMap.get(currentPath)!; // Get the node for the next iteration
    });
  });

  return root.children ?? []; // Return the top-level nodes (excluding the root)
}

export function parseFileTreeToNodes(
  hierarchy: DatasetVersionFileTree,
  parentDir: string = "/"
): DatasetVersionFileTreeNode[] {
  const isDirectory = (node: DatasetVersionFileTree | string): node is DatasetVersionFileTree => {
    return typeof node === "object" && node !== null && !(node instanceof Array);
  };

  const parseHierarchyToNode = (
    node: DatasetVersionFileTree | string,
    nodeName: string,
    currentDir: string
  ): DatasetVersionFileTreeNode => {
    if (isDirectory(node)) {
      let dir = currentDir === "/" ? `/${nodeName}` : `${currentDir}/${nodeName}`;
      return {
        name: nodeName,
        type: "directory",
        children: Object.keys(node).map(key => parseHierarchyToNode(node[key], key, dir)),
        parentDir: currentDir,
      };
    } else {
      return {
        name: nodeName,
        type: "file",
        parentDir: currentDir,
      };
    }
  };

  if (!isDirectory(hierarchy)) {
    throw new Error("The provided hierarchy is not a valid directory structure.");
  }

  return Object.keys(hierarchy).map(key => parseHierarchyToNode(hierarchy[key], key, parentDir));
}
