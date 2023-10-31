import { Injectable } from '@angular/core';
import {DashboardEnvironment} from "../../type/environment";
import {UserFileService} from "../user-file/user-file.service";

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private environments: DashboardEnvironment[] = [];
  private environmentOfWorkflow: Map<number, number> = new Map();

  constructor(private userFileService: UserFileService) {}

  updateEnvironmentWithFiles(eid: number): void {
    this.userFileService.getFileList().subscribe(
      (fileArr) => {
        let filePaths: string[] = [];
        for (let i = 0; i < fileArr.length; i++) {
          const f = fileArr[i];
          filePaths.push(f.ownerEmail + "/" + f.file.name);
        }

        this.environments[eid].environment.inputs = this.environments[eid].environment.inputs.concat(filePaths);
        console.log("Input Arr: ", this.environments[eid].environment.inputs)
      }
    );
  }

  updateAllEnvironmentsWithFiles(): void {
    this.userFileService.getFileList().subscribe(
      (fileArr) => {
        let filePaths: string[] = [];
        for (let i = 0; i < fileArr.length; i++) {
          const f = fileArr[i];
          filePaths.push(f.ownerEmail + "/" + f.file.name);
        }

        for (let i = 0; i < this.environments.length; i++) {
          this.environments[i].environment.inputs = this.environments[i].environment.inputs.concat(filePaths);
        }
      }
    );
  }

  doesWorkflowHaveEnvironment(wid: number): boolean {
    return this.environmentOfWorkflow.has(wid);
  }

  // Create: Add a new environment
  addEnvironment(environment: DashboardEnvironment): number {
    const eid = this.environments.length;
    environment.environment.eid = eid; // Set the eid as the index
    this.environments.push(environment);

    this.updateEnvironmentWithFiles(eid);
    return eid;
  }

  getEnvironmentIdentifiers(): Map<number, string> {
    const res: Map<number, string> = new Map();
    for (let i = 0; i < this.environments.length; i++) {
      const env = this.environments[i];
      res.set(env.environment.eid, env.environment.name);
    }

    return res;
  }

  getAllEnvironments(): DashboardEnvironment[] {
    return this.environments;
  }

  // Read: Get an environment by its index (eid)
  getEnvironmentByIndex(index: number): DashboardEnvironment | null {
    if (index >= 0 && index < this.environments.length) {
      return this.environments[index];
    }
    return null; // Return null if index out of bounds
  }

  // Update: Modify an existing environment by its index (eid)
  updateEnvironment(index: number, updatedEnvironment: DashboardEnvironment): void {
    if (index >= 0 && index < this.environments.length) {
      this.environments[index] = updatedEnvironment;
    } else {
      throw new Error('Environment index out of bounds');
    }
  }

  // Delete: Remove an environment by its index (eid)
  deleteEnvironment(index: number): void {
    if (index >= 0 && index < this.environments.length) {
      this.environments.splice(index, 1);
      // Re-assign EIDs for subsequent items
      for (let i = index; i < this.environments.length; i++) {
        this.environments[i].environment.eid = i;
      }
    } else {
      throw new Error('Environment index out of bounds');
    }
  }

  addEnvironmentOfWorkflow(wid: number, eid: number): void {
    if (this.environmentOfWorkflow.has(wid)) {
      throw new Error('Workflow ID already exists in the map.');
    }

    if (eid < 0 || eid >= this.environments.length) {
      throw new Error('Environment ID out of bounds.');
    }

    this.environmentOfWorkflow.set(wid, eid);
  }

  switchEnvironmentOfWorkflow(wid: number, eid: number): void {
    if (!this.environmentOfWorkflow.has(wid)) {
      throw new Error('Workflow ID does not exist in the map.');
    }

    if (eid < 0 || eid >= this.environments.length) {
      throw new Error('Environment ID out of bounds.');
    }

    this.environmentOfWorkflow.set(wid, eid);
  }

  // You might also want to add methods to get the environment of a workflow or to delete a mapping, etc.
  getEnvironmentOfWorkflow(wid: number): number | undefined {
    return this.environmentOfWorkflow.get(wid);
  }

  deleteEnvironmentOfWorkflow(wid: number): void {
    this.environmentOfWorkflow.delete(wid);
  }

}
