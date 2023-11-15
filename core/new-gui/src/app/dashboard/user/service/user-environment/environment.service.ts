import {Injectable} from '@angular/core';
import {DashboardEnvironment, Environment} from "../../type/environment";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {HttpClient} from "@angular/common/http";
import {WorkflowEnvironmentService} from "../../../../common/service/workflow-environment/workflow-environment.service";
import next from "ajv/dist/vocabularies/next";
import {Observable, of, throwError} from "rxjs";
import {catchError, filter, map} from "rxjs/operators";
import {AppSettings} from "../../../../common/app-setting";
import {InputOfEnvironment} from "../../type/input_of_environment";

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

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private workflowEnvironmentService: WorkflowEnvironmentService) {
  }


  // Create: Add a new environment
  addEnvironment(environment: Environment): Observable<DashboardEnvironment> {
    return this.http
      .post<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_CREATE_URL}`, {
        name: environment.name,
        description: environment.description,
        uid: environment.uid,
      })
      .pipe()
  }

  retrieveEnvironments(): Observable<DashboardEnvironment[]> {
    // TODO: finish this
    return this.http
      .get<DashboardEnvironment[]>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}`)
      .pipe()
  }

  getAllEnvironments(): DashboardEnvironment[] {
    return this.environments;
  }

  // Read: Get an environment by its index (eid)
  retrieveEnvironmentByEid(eid: number): Observable<null | DashboardEnvironment> {
    return this.http
      .get<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}`)
      .pipe(
        catchError(error => {
          // Handle HTTP errors, potentially return Observable.of(null) or throw
          return throwError(error);
        }),
        map(response => {
          // If response is empty or null (considering backend sends 204 or empty object for None)
          if (!response) {
            return null;
          }
          return response
        })
      )
  }

  addDatasetToEnvironment(input: InputOfEnvironment): Observable<Response> {
    const eid = input.eid;

    return this.http
      .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_INPUT_ADD_URL}`, {
        eid: input.eid,
        did: input.did,
        versionDescriptor: ""
      })
  }

  // Delete: Remove an environment by its index (eid)
  deleteEnvironments(eids: number[]): Observable<Response> {
    return this.http
      .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_DELETE_URL}`, {
        eids: eids
      })
  }
}
