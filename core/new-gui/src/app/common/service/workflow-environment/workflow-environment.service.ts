import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {NotificationService} from "../notification/notification.service";
import {Observable, throwError} from "rxjs";
import {DashboardEnvironment, Environment} from "../../../dashboard/user/type/environment";
import {AppSettings} from "../../app-setting";
import {WORKFLOW_PERSIST_URL} from "../workflow-persist/workflow-persist.service";
import {catchError, map} from "rxjs/operators";

export const WORKFLOW_ENVIRONMENT_BASE_URL = "environment"

export const WORKFLOW_ENVIRONMENT_RETRIEVE_URL = WORKFLOW_ENVIRONMENT_BASE_URL;

export const WORKFLOW_ENVIRONMENT_BIND_URL = WORKFLOW_ENVIRONMENT_BASE_URL + "/bind"

export const WORKFLOW_ENVIRONMENT_UNBIND_URL = WORKFLOW_ENVIRONMENT_BASE_URL + "/unbind"


@Injectable({
    providedIn: "root",
})
export class WorkflowEnvironmentService {
    constructor(
        private http: HttpClient,
        private notificationService: NotificationService
    ) {
    }

    public retrieveEnvironmentOfWorkflow(wid: number): Observable<DashboardEnvironment | null> {
        return this.http.get<DashboardEnvironment>(`${AppSettings.getApiEndpoint()}/${wid}/${WORKFLOW_ENVIRONMENT_RETRIEVE_URL}`)
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
            );
    }

    public bindWorkflowWithEnvironment(wid: number, eid: number): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${wid}/${WORKFLOW_ENVIRONMENT_BIND_URL}`, {
                eid: eid
            })
    }


    public unbindWorkflowWithEnvironment(wid: number): Observable<Response> {
        return this.http
            .post<Response>(`${AppSettings.getApiEndpoint()}/${wid}/${WORKFLOW_ENVIRONMENT_BIND_URL}`, {})
    }
}
