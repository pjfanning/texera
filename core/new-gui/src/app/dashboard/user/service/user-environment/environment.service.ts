import {Injectable} from '@angular/core';
import {DashboardEnvironment, DatasetOfEnvironment, DatasetOfEnvironmentDetails, Environment} from "../../type/environment";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {HttpClient} from "@angular/common/http";
import {WorkflowEnvironmentService} from "../../../../common/service/workflow-environment/workflow-environment.service";
import next from "ajv/dist/vocabularies/next";
import {Observable, of, throwError} from "rxjs";
import {catchError, filter, map} from "rxjs/operators";
import {AppSettings} from "../../../../common/app-setting";
import {Dataset} from "../../../../common/type/dataset";
import {DashboardDataset} from "../../type/dashboard-dataset.interface";

export const ENVIRONMENT_BASE_URL = "environment";
export const ENVIRONMENT_CREATE_URL = ENVIRONMENT_BASE_URL + "/create"
export const ENVIRONMENT_DELETE_URL = ENVIRONMENT_BASE_URL + "/delete"
export const ENVIRONMENT_LINK_WORKFLOW = "/linkWorkflow"
export const ENVIRONMENT_DATASET_RETRIEVAL_URL = "/dataset"
export const ENVIRONMENT_DATASET_DETAILS_RETRIEVAL_URL = ENVIRONMENT_DATASET_RETRIEVAL_URL + "/details"


export const ENVIRONMENT_DATASET_ADD_URL = ENVIRONMENT_DATASET_RETRIEVAL_URL + "/add"

export const ENVIRONMENT_DATASET_REMOVE_URL = ENVIRONMENT_DATASET_RETRIEVAL_URL + "/remove"

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
    createEnvironment(environment: Environment): Observable<DashboardEnvironment> {
        return this.http
            .post<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_CREATE_URL}`, {
                name: environment.name,
                description: environment.description,
                uid: environment.uid,
            })
            .pipe()
    }

    retrieveEnvironmentByEid(eid: number): Observable<DashboardEnvironment> {
        return this.http
            .get<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}`)
            .pipe(
                catchError(error => {
                    // Handle HTTP errors, potentially return Observable.of(null) or throw
                    return throwError(error);
                }),
                map(response => {
                    return response
                })
            )
    }

    addDatasetToEnvironment(eid: number, did: number): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_DATASET_ADD_URL}`, {
                did: did
            })
    }

    removeDatasetFromEnvironment(eid: number, did: number): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_DATASET_REMOVE_URL}`, {
                did: did
            })
    }

    retrieveDatasetsOfEnvironment(eid: number): Observable<DatasetOfEnvironment[]> {
        return this.http
            .get<DatasetOfEnvironment[]>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_DATASET_RETRIEVAL_URL}`)
    }

    retrieveDatasetsOfEnvironmentDetails(eid: number): Observable<DatasetOfEnvironmentDetails[]> {
        return this.http
            .get<DatasetOfEnvironmentDetails[]>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_DATASET_DETAILS_RETRIEVAL_URL}`)
    }

    // Delete: Remove an environment by its index (eid)
    deleteEnvironments(eids: number[]): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_DELETE_URL}`, {
                eids: eids
            })
    }

    linkWorkflowToEnvironment(wid: number, eid: number): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${ENVIRONMENT_BASE_URL}/${eid}/${ENVIRONMENT_LINK_WORKFLOW}`, {
                wid: wid
            })
    }
}
