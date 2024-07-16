import { Component, HostListener } from "@angular/core";
import { ReportPrintService } from "./workspace/component/report-print/report-print.service";
import { ResultExportResponse } from "./workspace/types/workflow-websocket.interface";
import { WorkflowActionService } from "./workspace/service/workflow-graph/model/workflow-action.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

@Component({
  selector: "texera-root",
  templateUrl: "./app.component.html",
})
export class AppComponent {
  results: ResultExportResponse[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private reportPrintService: ReportPrintService,
    private workflowActionService: WorkflowActionService
  ) {}

  @HostListener("window:beforeprint", ["$event"])
  onBeforePrint(event: Event) {
    this.prepareAndPrint();
    event.preventDefault();  // 阻止默认的打印行为
  }

  prepareAndPrint() {
    this.printAllResults(() => {
      setTimeout(() => window.print(), 1000);  // 确保获取结果后触发打印
    });
  }

  printAllResults(callback?: () => void) {
    const operatorIds = this.getOperatorIds(); // 获取所有操作符的ID

    this.reportPrintService.getAllOperatorResults(operatorIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe(results => {
        console.log(results);
        this.results = results;
        if (callback) {
          callback();
        }
      });
  }

  getOperatorIds(): string[] {
    // 获取所有操作符的ID逻辑
    return this.workflowActionService.getTexeraGraph().getAllOperators().map(op => op.operatorID);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
