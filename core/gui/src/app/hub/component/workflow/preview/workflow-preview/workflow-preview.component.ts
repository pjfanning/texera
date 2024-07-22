import {AfterViewInit, Component, HostListener, OnDestroy} from '@angular/core';
import * as joint from "jointjs";
import {WorkflowActionService} from "../../../../../workspace/service/workflow-graph/model/workflow-action.service";
import {MAIN_CANVAS} from "../../../../../workspace/component/workflow-editor/workflow-editor.component";
import {untilDestroyed} from "@ngneat/until-destroy";

@Component({
  selector: "workflow-preview",
  templateUrl: "./workflow-preview.component.html",
  styleUrls: ["./workflow-preview.component.css"]
})
export class WorkflowPreviewComponent implements AfterViewInit, OnDestroy {
  scale = 0;
  paper!: joint.dia.Paper;
  dragging = false;
  hidden = false;

  private isDragging = false;
  private lastMousePosition = { x: 0, y: 0 };

  constructor(private workflowActionService: WorkflowActionService) {}

  ngAfterViewInit() {
    const map = document.getElementById("workflow-editor")!;
    this.scale = 2 * map.offsetWidth / (MAIN_CANVAS.xMax - MAIN_CANVAS.xMin);
    this.paper = new joint.dia.Paper({
      el: map,
      model: this.workflowActionService.getJointGraphWrapper().jointGraph,
      background: { color: "#F6F6F6" },
      interactive: false,
      width: map.offsetWidth,
      height: map.offsetHeight,
    })
    this.paper
      .scale(this.scale)
      .translate(-MAIN_CANVAS.xMin * this.scale, -MAIN_CANVAS.yMin * this.scale);
    this.hidden = JSON.parse(localStorage.getItem("workflow-preview") as string) || false;
    map.addEventListener("mousedown", this.onMouseDown.bind(this));
    map.addEventListener("mousemove", this.onMouseMove.bind(this));
    map.addEventListener("mouseup", this.onMouseUp.bind(this));
    map.addEventListener("wheel", this.onMouseWheel.bind(this));
  }

  @HostListener("window:beforeunload")
  ngOnDestroy(): void {
    localStorage.setItem("workflow-preview", JSON.stringify(this.hidden));
    const map = document.getElementById("workflow-editor")!;
    map.removeEventListener("mousedown", this.onMouseDown.bind(this));
    map.removeEventListener("mousemove", this.onMouseMove.bind(this));
    map.removeEventListener("mouseup", this.onMouseUp.bind(this));
    map.removeEventListener("wheel", this.onMouseWheel.bind(this));
  }

  onDrag(event: any) {
    console.log("dragging")
    this.paper.translate(
      this.paper.translate().tx + -event.event.movementX / this.scale,
      this.paper.translate().ty + -event.event.movementY / this.scale
    );
  }

  onMouseDown(event: MouseEvent) {
    this.isDragging = true;
    this.lastMousePosition = { x: event.clientX, y: event.clientY };
  }

  onMouseMove(event: MouseEvent) {
    if (!this.isDragging) {
      return;
    }
    console.log("moving")
    const dx = event.clientX - this.lastMousePosition.x;
    const dy = event.clientY - this.lastMousePosition.y;

    this.paper.translate(
      this.paper.translate().tx + dx / 5 / this.scale,
      this.paper.translate().ty + dy / 5 / this.scale
    );

    this.lastMousePosition = { x: event.clientX, y: event.clientY };
  }

  onMouseUp(event: MouseEvent) {
    this.isDragging = false;
  }

  onMouseWheel(event: WheelEvent) {
    event.preventDefault();

    const delta = event.deltaY > 0 ? -0.1 : 0.1;
    const newScale = this.paper.scale().sx + delta;

    if (newScale > 0.2 && newScale < 3) {
      const currentScale = this.paper.scale().sx;
      const mousePos = { x: event.clientX, y: event.clientY };
      const offset = this.paper.translate();
      const newOffsetX = (mousePos.x - offset.tx) / currentScale * newScale - (mousePos.x - offset.tx);
      const newOffsetY = (mousePos.y - offset.ty) / currentScale * newScale - (mousePos.y - offset.ty);

      this.paper.scale(newScale, newScale);
      this.paper.translate(offset.tx - newOffsetX, offset.ty - newOffsetY);
    }
  }

}
