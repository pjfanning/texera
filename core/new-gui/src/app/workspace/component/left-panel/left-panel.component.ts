import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DynamicComponentConfig } from "../../../common/type/dynamic-component-config";
import { OperatorMenuFrameComponent } from "./operator-menu-frame/operator-menu-frame.component";
import { VersionsFrameComponent } from "./versions-frame/versions-frame.component";
import {
  OPEN_VERSIONS_FRAME_EVENT,
  WorkflowVersionService,
} from "../../../dashboard/user/service/workflow-version/workflow-version.service";
import {
  DISPLAY_WORKFLOW_EXECUTION_REPLAY,
  ReplayWorkflowService
} from "../../service/replay-workflow/replay-workflow.service";
import {merge} from "rxjs";
import {ReplayDisplayComponent} from "./replay-display/replay-display.component";

export type LeftFrameComponent = OperatorMenuFrameComponent | VersionsFrameComponent | ReplayDisplayComponent;
export type LeftFrameComponentConfig = DynamicComponentConfig<LeftFrameComponent>;

@UntilDestroy()
@Component({
  selector: "texera-left-panel",
  templateUrl: "./left-panel.component.html",
  styleUrls: ["./left-panel.component.scss"],
})
export class LeftPanelComponent implements OnInit {
  frameComponentConfig?: LeftFrameComponentConfig;

  constructor(private workflowVersionService: WorkflowVersionService,
              public replayWorkflowService: ReplayWorkflowService) {}

  ngOnInit(): void {
    this.registerVersionDisplayEventsHandler();
    this.switchFrameComponent({
      component: OperatorMenuFrameComponent,
      componentInputs: {},
    });
  }

  switchFrameComponent(targetConfig?: LeftFrameComponentConfig): void {
    if (
      this.frameComponentConfig?.component === targetConfig?.component &&
      this.frameComponentConfig?.componentInputs === targetConfig?.componentInputs
    ) {
      return;
    }

    this.frameComponentConfig = targetConfig;
  }

  registerVersionDisplayEventsHandler(): void {
    merge(this.workflowVersionService
      .workflowVersionsDisplayObservable(), this.replayWorkflowService.displayWorkflowReplayStream())
      .pipe(untilDestroyed(this))
      .subscribe(event => {
        const isDisplayWorkflowVersions =event===OPEN_VERSIONS_FRAME_EVENT;
        const isDisplayWorkflowReplay = event === DISPLAY_WORKFLOW_EXECUTION_REPLAY;
        if (isDisplayWorkflowVersions) {
          this.switchFrameComponent({
            component: VersionsFrameComponent,
          });
        } if (isDisplayWorkflowReplay) {
          this.switchFrameComponent({
            component: ReplayDisplayComponent,
          });
        }else {
          // go back to operator menu
          this.switchFrameComponent({
            component: OperatorMenuFrameComponent,
          });
        }
      });
  }
}
