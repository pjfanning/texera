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

  public getRecommendedWorkflowEditions(
    opId: string,
    workflowContent: WorkflowContent
  ): Observable<WorkflowEdition[]> {
    return new Observable<WorkflowEdition[]>((observer) => {
      if (opId.includes("CSVFileScan")) {
        // For CSVFileScan, recommend Projection and Limit
        const recommendations: WorkflowEdition[] = [
          new AddOpAndLinksEdition(
            workflowContent,
            "Projection",
            this.getProjectionProperties([
              { originalAttribute: "tweet_id", alias: "" },
            ]),
            [
              {
                sourceOpId: opId,
                sourcePortId: "output-0",
                targetPortId: "input-0",
              },
            ],
            "Keep only tweet_id and date attributes."
          ),
          new AddOpAndLinksEdition(
            workflowContent,
            "Limit",
            this.getLimitProperties(10),
            [
              {
                sourceOpId: opId,
                sourcePortId: "output-0",
                targetPortId: "input-0",
              },
            ],
            "Limit the results to 10 rows."
          ),
        ];

        observer.next(recommendations); // Emit the recommendations
      } else if (opId.includes("Projection")) {
        const recommendations: WorkflowEdition[] = [
          new AddOpAndLinksEdition(
            workflowContent,
            "Aggregate",
            this.getAggregateProperties([{
              aggFunction: "count",
              attribute: "tweet_id",
              resultAttribute: "#tweets"
            }], ["date"]),
            [
              {
                sourceOpId: opId,
                sourcePortId: "output-0",
                targetPortId: "input-0",
              },
            ],
            "Count number of tweets each month"
          ),
        ]
        observer.next(recommendations);
      } else {
        observer.next([]); // Emit an empty array if no recommendations
      }
      observer.complete(); // Mark the observable as complete
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

    console.log("send request: ", request);
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

  // functions to generate the properties
  public getCSVFileScanProperties(fileName: string): Record<string, any> {
    return {
      "fileName": fileName
    }
  }

  public getProjectionProperties(attributes: {originalAttribute: string; alias: string}[]): Record<string, any> {
    return {
      "attributes": attributes
    }
  }

  public getLimitProperties(limit: number): Record<string, any> {
    return {
      "limit": limit
    }
  }

  public getAggregateProperties(aggregations: {aggFunction: string; attribute: string; resultAttribute: string}[], groupByKeys: string[]) {
    return {
      aggregations: aggregations,
      groupByKeys: groupByKeys
    }
  }
}
