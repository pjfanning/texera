import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {NotificationService} from "../notification/notification.service";
import {Observable, throwError} from "rxjs";
import {DashboardEnvironment, Environment} from "../../../dashboard/user/type/environment";
import {AppSettings} from "../../app-setting";
import {WORKFLOW_BASE_URL, WORKFLOW_PERSIST_URL} from "../workflow-persist/workflow-persist.service";
import {catchError, map} from "rxjs/operators";

export const WORKFLOW_ENVIRONMENT_BASE_URL = "environment"
export const WORKFLOW_ENVIRONMENT_RETRIEVE_URL = WORKFLOW_ENVIRONMENT_BASE_URL;
export const WORKFLOW_ENVIRONMENT_BIND_URL = WORKFLOW_ENVIRONMENT_BASE_URL + "/bind"
export const WORKFLOW_ENVIRONMENT_UNBIND_URL = WORKFLOW_ENVIRONMENT_BASE_URL + "/unbind"

@Injectable({
  providedIn: "root",
})
export class WorkflowEnvironmentService {
  private environmentOfWorkflow: Map<number, number> = new Map();

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {
  }


  public doesWorkflowHaveEnvironment(wid: number): Observable<boolean> {
    return new Observable<boolean>(subscriber => {
      subscriber.next(this.environmentOfWorkflow.has(wid));
      subscriber.complete();
    })
  }

  public retrieveEnvironmentIdOfWorkflow(wid: number): Observable<number> {
    return new Observable<number>(subscriber => {
      const environmentId = this.environmentOfWorkflow.get(wid);

      if (environmentId === undefined) {
        subscriber.error(new Error("Environment ID not found for workflow ID " + wid));
      } else {
        subscriber.next(environmentId);
        subscriber.complete();
      }
    });
  }

  public bindWorkflowWithEnvironment(wid: number, eid: number): Observable<boolean> {
    return new Observable<boolean>(subscriber => {
      this.environmentOfWorkflow.set(wid, eid);
      subscriber.next(true);
      subscriber.complete()
    })
  }


  public unbindWorkflowWithEnvironment(wid: number): Observable<boolean> {
    return new Observable<boolean>(subscriber => {
      this.environmentOfWorkflow.delete(wid);
      subscriber.next(true);
      subscriber.complete()
    })
  }
}
