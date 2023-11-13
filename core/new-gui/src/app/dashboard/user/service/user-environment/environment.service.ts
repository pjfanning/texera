import { Injectable } from '@angular/core';
import {DashboardEnvironment} from "../../type/environment";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {HttpClient} from "@angular/common/http";
import {WorkflowEnvironmentService} from "../../../../common/service/workflow-environment/workflow-environment.service";
import next from "ajv/dist/vocabularies/next";
import {Observable, of} from "rxjs";
import {catchError, filter, map} from "rxjs/operators";
import {AppSettings} from "../../../../common/app-setting";

export const ENVIRONMENT_BASE_URL = "environment";
export const ENVIRONMENT_CREATE_URL = ENVIRONMENT_BASE_URL + "/create"
export const ENVIRONMENT_DELETE_URL = ENVIRONMENT_BASE_URL + "/delete"
export const ENVIRONMENT_INPUT_RETRIEVAL_URL = "/input"
export const ENVIRONMENT_INPUT_ADD_URL = ENVIRONMENT_INPUT_RETRIEVAL_URL + "/add"



@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private environments: DashboardEnvironment[] = [];
  private environmentOfWorkflow: Map<number, number> = new Map();

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private workflowEnvironmentService: WorkflowEnvironmentService) {}

  doesWorkflowHaveEnvironment(wid: number): Observable<boolean> {
    return this.workflowEnvironmentService.retrieveEnvironmentOfWorkflow(wid).pipe(
        map(environment => {
          // If the environment is null, it means the workflow does not have an environment
          return environment !== null;
        }),
        catchError(error => {
          // Handle the error case, you might want to return false or re-throw the error
          throw error
        })
    );
  }


  // Create: Add a new environment
  addEnvironment(environment: DashboardEnvironment): Observable<DashboardEnvironment> {
    return this.http
        .post<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_CREATE_URL}`, {
          name: environment.environment.name,
          description: environment.environment.description,
          uid: environment.environment.uid,
        })
        .pipe()
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
