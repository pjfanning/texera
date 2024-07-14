import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { AppSettings } from "../../../common/app-setting";
import { HubWorkflow } from "../../component/type/hub-workflow.interface";

export const BASE_URL = `${AppSettings.getApiEndpoint()}/hub/workflow`;

@Injectable({
  providedIn: "root",
})
export class HubWorkflowService {
  constructor(private http: HttpClient) {}

  public getWorkflowList(): Observable<HubWorkflow[]> {
    return this.http.get<HubWorkflow[]>(`${BASE_URL}/list`);
  }

}
