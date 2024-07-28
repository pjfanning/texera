import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { AppSettings } from "../../../common/app-setting";
import { HubWorkflow } from "../../component/type/hub-workflow.interface";

export const WORKFLOW_BASE_URL = `${AppSettings.getApiEndpoint()}/workflow`;

export interface PartialUser {
  name: string;
  color?: string;
  googleAvatar: string;
}

@Injectable({
  providedIn: "root",
})
export class HubWorkflowService {
  readonly BASE_URL: string = `${AppSettings.getApiEndpoint()}/hub/workflow`;

  constructor(private http: HttpClient) {}

  public getWorkflowCount(): Observable<number> {
    return this.http.get<number>(`${this.BASE_URL}/count`);
  }

  public getWorkflowList(): Observable<HubWorkflow[]> {
    return this.http.get<HubWorkflow[]>(`${this.BASE_URL}/list`);
  }

  public cloneWorkflow(wid: number): Observable<number> {
    return this.http.post<number>(`${WORKFLOW_BASE_URL}/clone/${wid}`, null);
  }

  public getUserName(wid: number): Observable<string>{
    const params = new HttpParams().set("wid", wid);
    return this.http.get<string>(`${this.BASE_URL}/user_name/`, { params, responseType: "text" as "json" })
  }

  public getUserInfo(wids: number[]): Observable<{ [key: number]: PartialUser }> {
    let params = new HttpParams();
    wids.forEach(wid => {
      params = params.append("wids", wid.toString());
    });
    return this.http.get<{ [key: number]: PartialUser }>(`${this.BASE_URL}/user_info`, { params });
  }

  }
