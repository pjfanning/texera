import { Component, Input, OnInit } from "@angular/core";

@Component({
  selector: "texera-result-comparison-tables-frame",
  templateUrl: "./result-comparison-tables-frame.component.html",
  styleUrls: ["./result-comparison-tables-frame.component.scss"]
})
export class ResultComparisonTablesFrameComponent implements OnInit {
  @Input() operatorId!: string;
  @Input() operatorId_to_compare!: string;
  constructor() {}

  ngOnInit(): void {
    console.log(this.operatorId);
    console.log(this.operatorId_to_compare);
  }
}

