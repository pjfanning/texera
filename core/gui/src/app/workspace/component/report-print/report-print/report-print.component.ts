import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { ResultExportResponse } from "../../../types/workflow-websocket.interface";

@Component({
  selector: "texera-report-print",
  templateUrl: "./report-print.component.html",
  styleUrls: ["./report-print.component.css"]
})
export class ReportPrintComponent implements OnChanges {

  @Input() results: ResultExportResponse[] = [];

  constructor() {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.results && this.results) {
      this.results.forEach(result => {
        // 记录导出的文件路径
        console.log(`Result exported to: ${result.message}`);
      });
    }
  }
}
