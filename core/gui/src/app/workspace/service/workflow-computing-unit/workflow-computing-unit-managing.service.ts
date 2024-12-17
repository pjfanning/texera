import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { AppSettings } from "../../../common/app-setting";
import {WorkflowComputingUnit} from "../../types/workflow-computing-unit";

export const COMPUTING_UNIT_BASE_URL = "computing-unit";
export const COMPUTING_UNIT_CREATE_URL = `${COMPUTING_UNIT_BASE_URL}/create`;
export const COMPUTING_UNIT_TERMINATE_URL = `${COMPUTING_UNIT_BASE_URL}/terminate`;
export const COMPUTING_UNIT_LIST_URL = `${COMPUTING_UNIT_BASE_URL}`;

@Injectable({
  providedIn: "root",
})
export class WorkflowComputingUnitManagingService {
  constructor(private http: HttpClient) {}

  /**
   * Create a new workflow computing unit (pod).
   * @param uid The user ID.
   * @param name The name for the computing unit.
   * @returns An Observable of the created WorkflowComputingUnit.
   */
  public createComputingUnit(uid: number, name: string): Observable<WorkflowComputingUnit> {
    const body = { uid, name };

    return this.http.post<WorkflowComputingUnit>(
      `${AppSettings.getApiEndpoint()}/${COMPUTING_UNIT_CREATE_URL}`,
      body
    );
  }

  /**
   * Terminate a computing unit (pod) by its URI.
   * @param podURI The URI of the pod to be terminated.
   * @returns An Observable of the server response.
   */
  public terminateComputingUnit(podURI: string): Observable<Response> {
    const body = { podURI };

    return this.http.post<Response>(
      `${AppSettings.getApiEndpoint()}/${COMPUTING_UNIT_TERMINATE_URL}`,
      body
    );
  }

  /**
   * List all active computing units.
   * @returns An Observable of a list of WorkflowComputingUnit.
   */
  public listComputingUnits(): Observable<WorkflowComputingUnit[]> {
    return this.http.get<WorkflowComputingUnit[]>(
      `${AppSettings.getApiEndpoint()}/${COMPUTING_UNIT_LIST_URL}`
    );
  }
}
