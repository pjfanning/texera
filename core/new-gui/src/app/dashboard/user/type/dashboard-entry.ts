import { DashboardFile } from "./dashboard-file.interface";
import { DashboardWorkflow } from "./dashboard-workflow.interface";
import { DashboardProject } from "./dashboard-project.interface";
import {DashboardEnvironment} from "./environment";

export class DashboardEntry {
  checked = false;
  get type(): "workflow" | "project" | "file" | "environment" {
    if ("workflow" in this.value) {
      return "workflow";
    } else if ("name" in this.value) {
      return "project";
    } else if ("ownerEmail" in this.value) {
      return "file";
    } else if ("environment" in this.value) {
      return "environment";
    }
    throw new Error("Unexpected type in DashboardEntry.");
  }
  get name(): string {
    if ("workflow" in this.value) {
      return this.value.workflow.name;
    } else if ("name" in this.value) {
      return this.project.name;
    } else if ("ownerEmail" in this.value) {
      return this.value.file.name;
    } else if ("environment" in this.value) {
      return this.value.environment.name;
    }
    throw new Error("Unexpected type in DashboardEntry.");
  }

  get creationTime(): number | undefined {
    if ("workflow" in this.value) {
      return this.value.workflow.creationTime;
    } else if ("name" in this.value) {
      return this.value.creationTime;
    } else if ("ownerEmail" in this.value) {
      return this.value.file.uploadTime;
    } else if ("environment" in this.value) {
      return this.value.environment.creationTime;
    }
    throw new Error("Unexpected type in DashboardEntry.");
  }

  get lastModifiedTime(): number | undefined {
    if ("workflow" in this.value) {
      return this.value.workflow.lastModifiedTime;
    } else if ("name" in this.value) {
      return this.value.creationTime;
    } else if ("ownerEmail" in this.value) {
      return this.value.file.uploadTime;
    } else if ("environment" in this.value) {
      return this.value.environment.creationTime;
    }
    throw new Error("Unexpected type in DashboardEntry.");
  }

  get project(): DashboardProject {
    if (!("name" in this.value)) {
      throw new Error("Value is not of type Workflow.");
    }
    return this.value;
  }

  get workflow(): DashboardWorkflow {
    if (!("workflow" in this.value)) {
      throw new Error("Value is not of type Workflow.");
    }
    return this.value;
  }

  get file(): DashboardFile {
    if (!("ownerEmail" in this.value)) {
      throw new Error("Value is not of type file.");
    }
    return this.value;
  }

  get environment(): DashboardEnvironment {
    if (!("environment" in this.value)) {
      throw new Error("Value is not of type Environment.");
    }
    return this.value;
  }

  constructor(public value: DashboardWorkflow | DashboardProject | DashboardFile | DashboardEnvironment) {}
}
