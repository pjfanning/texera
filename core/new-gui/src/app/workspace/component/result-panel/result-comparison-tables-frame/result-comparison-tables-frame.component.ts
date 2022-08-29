import { Component, Input, OnInit } from "@angular/core";
import { UntilDestroy } from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: "texera-result-comparison-tables-frame",
  templateUrl: "./result-comparison-tables-frame.component.html",
  styleUrls: ["./result-comparison-tables-frame.component.scss"],
})
export class ResultComparisonTablesFrameComponent {
  @Input() eIdToSink: Map<number, string> = new Map();
  @Input() leftPanelEid: number = -1;
  @Input() rightPanelEid: number = -1;

  constructor() {}
}
