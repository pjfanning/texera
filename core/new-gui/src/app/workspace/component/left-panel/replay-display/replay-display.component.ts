import { Component, OnInit } from "@angular/core";
import { ReplayWorkflowService } from "src/app/workspace/service/replay-workflow/replay-workflow.service";

@Component({
  selector: "replay-display",
  templateUrl: "./replay-display.component.html",
  styleUrls: ["./replay-display.component.scss"],
})
export class ReplayDisplayComponent implements OnInit {
  constructor(public replay: ReplayWorkflowService) {}

  ngOnInit(): void {}
}
