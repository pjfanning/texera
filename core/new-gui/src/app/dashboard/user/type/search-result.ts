import { DashboardFile } from "./dashboard-file.interface";
import { DashboardWorkflow } from "./dashboard-workflow.interface";
import { DashboardProject } from "./dashboard-project.interface";
import {DashboardEnvironment} from "./environment";

export interface SearchResultItem {
  resourceType: "workflow" | "project" | "file" | "environment";
  workflow?: DashboardWorkflow;
  project?: DashboardProject;
  file?: DashboardFile;
  environment?: DashboardEnvironment;
}

export interface SearchResult {
  results: SearchResultItem[];
  more: boolean;
}
