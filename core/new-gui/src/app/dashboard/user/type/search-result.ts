import { DashboardFile } from "./dashboard-file.interface";
import { DashboardWorkflow } from "./dashboard-workflow.interface";
import { DashboardProject } from "./dashboard-project.interface";
import {DashboardEnvironment} from "./environment";
import { DashboardDataset } from "./dashboard-dataset.interface";

export interface SearchResultItem {
  resourceType: "workflow" | "project" | "file" | "dataset" | "environment";
  workflow?: DashboardWorkflow;
  project?: DashboardProject;
  file?: DashboardFile;
  dataset?: DashboardDataset;
  environment?: DashboardEnvironment;
}

export interface SearchResult {
  results: SearchResultItem[];
  more: boolean;
}
