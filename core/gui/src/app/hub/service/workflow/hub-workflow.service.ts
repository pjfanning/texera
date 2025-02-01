import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { AppSettings } from "../../../common/app-setting";
import { DashboardWorkflow } from "../../../dashboard/type/dashboard-workflow.interface";

export const WORKFLOW_BASE_URL = `${AppSettings.getApiEndpoint()}/workflow`;

@Injectable({
  providedIn: "root",
})
export class HubWorkflowService {
  readonly BASE_URL: string = `${AppSettings.getApiEndpoint()}/hub/workflow`;

  constructor(private http: HttpClient) {}

  public getWorkflowCount(): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/count`);
  }

  public cloneWorkflow(wid: number): Observable<number> {
    return this.http.post<number>(`${WORKFLOW_BASE_URL}/clone/${wid}`, null);
  }

  public isWorkflowLiked(workflowId: number, userId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.BASE_URL}/isLiked`, {
      params: { workflowId: workflowId.toString(), userId: userId.toString() },
    });
  }

  public postLikeWorkflow(workflowId: number, userId: number): Observable<boolean> {
    return this.http.post<boolean>(`${this.BASE_URL}/like`, [workflowId, userId]);
  }

  public postUnlikeWorkflow(workflowId: number, userId: number): Observable<boolean> {
    return this.http.post<boolean>(`${this.BASE_URL}/unlike`, [workflowId, userId]);
  }

  public getLikeCount(wid: number): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/likeCount/${wid}`);
  }

  public getCloneCount(wid: number): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/cloneCount/${wid}`);
  }

  public getTopLovedWorkflows(): Observable<DashboardWorkflow[]> {
    return this.http.get<DashboardWorkflow[]>(`${this.BASE_URL}/topLovedWorkflows`);
  }

  public getTopClonedWorkflows(): Observable<DashboardWorkflow[]> {
    return this.http.get<DashboardWorkflow[]>(`${this.BASE_URL}/topClonedWorkflows`);
  }

  public postViewWorkflow(workflowId: number, userId: number): Observable<number> {
    return this.http.post<number>(`${this.BASE_URL}/view`, [workflowId, userId]);
  }

  public getViewCount(wid: number): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/viewCount/${wid}`);
  }
}
