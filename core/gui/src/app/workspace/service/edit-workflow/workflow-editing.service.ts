import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { WorkflowContent } from "../../../common/type/workflow";
import { AddOpAndLinksEdition, WorkflowEdition } from "../../types/workflow-editing.interface";
import { AppSettings } from "../../../common/app-setting";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

export const WORKFLOW_EDITING_BASE_URL = "workflow-editing";
export const WORKFLOW_EDITING_ADD_OP_URL = `${WORKFLOW_EDITING_BASE_URL}/add-operator-and-links`;

@Injectable({
  providedIn: "root",
})
export class WorkflowEditingService {
  constructor(private http: HttpClient) {}

  public getRecommendedWorkflowEditions(opId: String): Observable<WorkflowEdition[]> {
    // Return an empty list as an observable
    // TODO: hard code the map
    return new Observable<WorkflowEdition[]>((observer) => {
      observer.next([]); // Emit an empty list
      observer.complete(); // Signal completion
    });
  }

  /**
   * Public method to apply a WorkflowEdition.
   * Dispatches to the correct private method based on the type of WorkflowEdition.
   */
  public applyWorkflowEdition(edition: WorkflowEdition): Observable<WorkflowContent> {
    if (this.isAddOpAndLinksEdition(edition)) {
      return this.addOperatorAndLinks(edition);
    } else {
      const typeError = new Error("Unsupported WorkflowEdition type.");
      console.error(typeError.message);
      return throwError(() => typeError);
    }
  }

  /**
   * Private method to handle AddOpAndLinksEdition.
   */
  private addOperatorAndLinks(addOp: AddOpAndLinksEdition): Observable<WorkflowContent> {
    const request = {
      workflowContent: addOp.workflowContent,
      links: addOp.links,
      operatorProperties: addOp.operatorProperties,
      operatorType: addOp.operatorType,
    };

    return this.http
      .post<WorkflowContent>(`${AppSettings.getApiEndpoint()}/${WORKFLOW_EDITING_ADD_OP_URL}`, request)
      .pipe(
        catchError((error) => {
          console.error("Error adding operator and links:", error);
          return throwError(() => new Error("HTTP error occurred while adding operator and links."));
        })
      );
  }

  /**
   * Type guard for AddOpAndLinksEdition.
   */
  private isAddOpAndLinksEdition(addOp: WorkflowEdition): addOp is AddOpAndLinksEdition {
    return (
      "workflowContent" in addOp &&
      "links" in addOp &&
      "operatorProperties" in addOp &&
      "operatorType" in addOp
    );
  }
}
