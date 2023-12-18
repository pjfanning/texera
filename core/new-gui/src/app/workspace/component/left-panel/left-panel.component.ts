import { Component, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { OperatorMenuComponent } from "./operator-menu/operator-menu.component";
import { VersionsListComponent } from "./versions-list/versions-list.component";
import { ComponentType } from "@angular/cdk/overlay";
import {
  OPEN_VERSIONS_FRAME_EVENT,
  WorkflowVersionService,
} from "../../../dashboard/user/service/workflow-version/workflow-version.service";
import { NzResizeEvent } from "ng-zorro-antd/resizable";
import {TimeTravelComponent} from "./time-travel/time-travel.component";
import {OPEN_TIMETRAVEL_FRAME_EVENT, TimeTravelService} from "../../service/time-travel/time-travel.service";
import {merge} from "rxjs";

@UntilDestroy()
@Component({
  selector: "texera-left-panel",
  templateUrl: "left-panel.component.html",
  styleUrls: ["left-panel.component.scss"],
})
export class LeftPanelComponent implements OnInit {
  currentComponent: ComponentType<OperatorMenuComponent | VersionsListComponent | TimeTravelComponent>;
  screenWidth = window.innerWidth;
  width = 200;
  id = -1;
  disabled = false;

  onResize({ width }: NzResizeEvent): void {
    cancelAnimationFrame(this.id);
    this.id = requestAnimationFrame(() => {
      this.width = width!;
    });
  }

  constructor(private workflowVersionService: WorkflowVersionService, private timetravelService:TimeTravelService) {
    this.currentComponent = OperatorMenuComponent;
  }

  ngOnInit(): void {
    this.registerVersionDisplayEventsHandler();
  }

  registerVersionDisplayEventsHandler(): void {
    merge(this.workflowVersionService
      .workflowVersionsDisplayObservable(),
      this.timetravelService.timetravelDisplayObservable())
      .pipe(untilDestroyed(this))
      .subscribe(
        evt =>{
          switch (evt) {
            case OPEN_VERSIONS_FRAME_EVENT:
              this.currentComponent = VersionsListComponent;
              break;
            case OPEN_TIMETRAVEL_FRAME_EVENT:
              this.currentComponent = TimeTravelComponent;
              break;
            default:
              this.currentComponent = OperatorMenuComponent;
          }
        }
      );
  }
}
