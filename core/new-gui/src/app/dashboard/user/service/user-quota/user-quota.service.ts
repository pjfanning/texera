import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { AppSettings } from "src/app/common/app-setting";
import { File, Workflow, mongoExecution } from "src/app/common/type/user";

export const USER_BASE_URL = `${AppSettings.getApiEndpoint()}/quota`;
export const USER_CREATED_FILES = `${USER_BASE_URL}/uploaded_files`
export const USER_CREATED_WORKFLOWS = `${USER_BASE_URL}/created_workflows`
export const USER_ACCESS_WORKFLOWS = `${USER_BASE_URL}/access_workflows`
export const USER_ACCESS_FILES = `${USER_BASE_URL}/access_files`
export const USER_MONGODB_SIZE = `${USER_BASE_URL}/mongodb_size`

@Injectable({
  providedIn: "root",
})
export class UserQuotaService {
  constructor(private http: HttpClient) {}

  public getUploadedFiles(): Observable<ReadonlyArray<File>> {
    return this.http.get<ReadonlyArray<File>>(`${USER_CREATED_FILES}`);
  }

  public getCreatedWorkflows(): Observable<ReadonlyArray<Workflow>> {
    return this.http.get<ReadonlyArray<Workflow>>(`${USER_CREATED_WORKFLOWS}`);
  }

  public getAccessFiles(): Observable<ReadonlyArray<number>> {
    return this.http.get<ReadonlyArray<number>>(`${USER_ACCESS_FILES}`);
  }

  public getAccessWorkflows(): Observable<ReadonlyArray<number>> {
    return this.http.get<ReadonlyArray<number>>(`${USER_ACCESS_WORKFLOWS}`);
  }

  public getMongoDBs(): Observable<ReadonlyArray<mongoExecution>> {
    return this.http.get<ReadonlyArray<mongoExecution>>(`${USER_MONGODB_SIZE}`);
  }
}
