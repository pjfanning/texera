import { Injectable } from "@angular/core";
import { WorkflowWebsocketService } from "../../service/workflow-websocket/workflow-websocket.service";
import { Observable, forkJoin } from "rxjs";
import { ResultExportResponse } from "../../types/workflow-websocket.interface";
import {WorkflowActionService} from "../../service/workflow-graph/model/workflow-action.service";

@Injectable({
  providedIn: "root"
})
export class ReportPrintService {

  constructor(
    private workflowWebsocketService: WorkflowWebsocketService,
    private workflowActionService: WorkflowActionService
  ) {}

  getAllOperatorResults(operatorIds: string[]): Observable<ResultExportResponse[]> {
    const requests = operatorIds.map(id => this.getResultForOperator(id));
    return forkJoin(requests);
  }

  private getResultForOperator(operatorId: string): Observable<ResultExportResponse> {
    return new Observable(observer => {
      const workflow = this.workflowActionService.getWorkflow();
      if (!workflow.wid) {
        throw new Error("Workflow ID is undefined");
      }
      const operator = this.workflowActionService.getTexeraGraph().getOperator(operatorId);

      const payload = {
        exportType: "json",
        workflowId: workflow.wid,
        workflowName: workflow.name,
        operatorId: operatorId,
        operatorName: operator.customDisplayName ?? operator.operatorType,
        datasetIds: [] as number[]  // 如果有数据集 ID 列表可以传递，否则留空
      };

      this.workflowWebsocketService.send("ResultExportRequest", payload);
      this.workflowWebsocketService.subscribeToEvent("ResultExportResponse")
        .subscribe((response: ResultExportResponse) => {
          observer.next(response);
          observer.complete();
        });
    });
  }
}
