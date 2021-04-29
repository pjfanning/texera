import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ExecutionState, OperatorState, OperatorStatistics, IncrementalOutputResult, ResultObject } from '../../types/execute-workflow.interface';
import { ExecuteWorkflowService } from '../execute-workflow/execute-workflow.service';
import { WorkflowActionService } from '../workflow-graph/model/workflow-action.service';
import { WorkflowWebsocketService } from '../workflow-websocket/workflow-websocket.service';
import { assertNever } from 'src/app/common/util/assert';

@Injectable({
  providedIn: 'root'
})
export class WorkflowStatusService {
  // status is responsible for passing websocket responses to other components
  private statusSubject = new Subject<Record<string, OperatorStatistics>>();
  private currentStatus: Record<string, OperatorStatistics> = {};

  private resultUpdateStream = new Subject<Record<string, IncrementalOutputResult>>();

  // current incremental result
  // for SET_SNAPSHOT output mode:  latested output snapshot
  // for SET_DELTA    output mode:  accumulated delta
  //                                delta no retraction   - same as snapshot
  //                                delta with retraction - accumulated delta is not compacted
  // When resultUpdateStream emits a new update event, the update is already applied on the result set
  private currentIncrementalResult: Record<string, ResultObject> = {};

  constructor(
    private workflowActionService: WorkflowActionService,
    private workflowWebsocketService: WorkflowWebsocketService,
    private executeWorkflowService: ExecuteWorkflowService
  ) {
    if (!environment.executionStatusEnabled) {
      return;
    }
    this.getStatusUpdateStream().subscribe(event => this.currentStatus = event);

    this.workflowWebsocketService.websocketEvent().subscribe(event => {
      if (event.type !== 'WebWorkflowStatusUpdateEvent') {
        return;
      }
      this.statusSubject.next(event.operatorStatistics);
    });

    this.workflowWebsocketService.websocketEvent().subscribe(event => {
      if (event.type !== 'WebWorkflowResultUpdateEvent') {
        return;
      }

      // apply the update on the result set based on SET_SNAPSHOT and SET_DELTA semantics
      Object.entries(event.operatorResults).forEach(e => {
        const opID = e[0];
        const resultUpdate = e[1];

        if (resultUpdate.outputMode === 'SET_SNAPSHOT') {
          this.currentIncrementalResult[opID] = resultUpdate.result;
        } else if (resultUpdate.outputMode === 'SET_DELTA') {
          const combinedResult = [];
          combinedResult.push(this.currentIncrementalResult[opID]?.table ?? []);
          combinedResult.push(resultUpdate.result.table);
          let rowCount = 0;
          rowCount += this.currentIncrementalResult[opID]?.totalRowCount ?? 0;
          rowCount += resultUpdate.result.totalRowCount;
          this.currentIncrementalResult[opID] = {
            operatorID: resultUpdate.result.operatorID,
            chartType: resultUpdate.result.chartType,
            table: combinedResult,
            totalRowCount: rowCount,
          };
        } else {
          const _exhaustiveCheck: never = resultUpdate.outputMode;
        }
      });

      this.resultUpdateStream.next(event.operatorResults);
    });

    this.executeWorkflowService.getExecutionStateStream().subscribe(event => {
      if (event.current.state === ExecutionState.WaitingToRun) {
        const initialStatistics: Record<string, OperatorStatistics> = {};
        this.workflowActionService.getTexeraGraph().getAllOperators().forEach(op => {
          initialStatistics[op.operatorID] = {
            operatorState: OperatorState.Initializing,
            aggregatedInputRowCount: 0,
            aggregatedOutputRowCount: 0,
          };
        });
        this.statusSubject.next(initialStatistics);
      }
    });
  }

  public getStatusUpdateStream(): Observable<Record<string, OperatorStatistics>> {
    return this.statusSubject.asObservable();
  }

  public getCurrentStatus(): Record<string, OperatorStatistics> {
    return this.currentStatus;
  }

  public getResultUpdateStream(): Observable<Record<string, IncrementalOutputResult>> {
    return this.resultUpdateStream.asObservable();
  }

  public getCurrentIncrementalResult(): Record<string, IncrementalOutputResult> {
    return this.currentIncrementalResult;
  }

}
