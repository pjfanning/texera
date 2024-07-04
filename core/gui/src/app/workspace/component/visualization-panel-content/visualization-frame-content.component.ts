import { AfterContentInit, Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { DomSanitizer } from "@angular/platform-browser";
import { WorkflowResultService } from "../../service/workflow-result/workflow-result.service";
import { auditTime, filter } from "rxjs/operators";
import { untilDestroyed, UntilDestroy } from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: "texera-visualization-panel-content",
  templateUrl: "./visualization-frame-content.component.html",
  styleUrls: ["./visualization-frame-content.component.scss"],
})
export class VisualizationFrameContentComponent implements AfterContentInit, OnChanges {
  // operatorId: string = inject(NZ_MODAL_DATA).operatorId;
  @Input() operatorId?: string;
  // progressive visualization update and redraw interval in milliseconds
  public static readonly UPDATE_INTERVAL_MS = 2000;
  htmlData: any = "";

  constructor(
    private workflowResultService: WorkflowResultService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.operatorId) {
      const previousValue = changes.operatorId.previousValue;
      const currentValue = changes.operatorId.currentValue;
      const firstChange = changes.operatorId.firstChange;

      console.log("=======");
      console.log('operatorId changed:', { previousValue, currentValue, firstChange });
      console.log("=======");
      
      this.drawChart();  // Ensure drawChart is called on every operatorId change
    }
  }

  ngAfterContentInit() {
    // attempt to draw chart immediately
    this.drawChart();

    this.workflowResultService
      .getResultUpdateStream()
      .pipe(auditTime(VisualizationFrameContentComponent.UPDATE_INTERVAL_MS))
      .pipe(filter(rec => this.operatorId !== undefined && rec[this.operatorId] !== undefined))
      .pipe(untilDestroyed(this))
      .subscribe(() => {
        this.updateChart();
      });
  }

  drawChart() {
    console.log("000")
    if (!this.operatorId) {
      console.log("1")
      return;
    }
    const operatorResultService = this.workflowResultService.getResultService(this.operatorId);
    if (!operatorResultService) {
      console.log("2")
      return;
    }
    const data = operatorResultService.getCurrentResultSnapshot();
    if (!data) {
      console.log("3")
      return;
    }

    const parser = new DOMParser();
    const lastData = data[data.length - 1];
    const doc = parser.parseFromString(Object(lastData)["html-content"], "text/html");

    doc.documentElement.style.height = "100%";
    doc.body.style.height = "95%";

    const firstDiv = doc.body.querySelector("div");
    if (firstDiv) firstDiv.style.height = "100%";

    const serializer = new XMLSerializer();
    const newHtmlString = serializer.serializeToString(doc);

    this.htmlData = this.sanitizer.bypassSecurityTrustHtml(newHtmlString); // Update without checking to initialize or change operator
    console.log("4");
  }

  updateChart() {
    console.log("Updating chart");
    if (!this.operatorId) {
      console.log("1")
      return;
    }
    const operatorResultService = this.workflowResultService.getResultService(this.operatorId);
    if (!operatorResultService) {
      console.log("2")
      return;
    }
    const data = operatorResultService.getCurrentResultSnapshot();
    if (!data) {
      console.log("3")
      return;
    }

    const parser = new DOMParser();
    const lastData = data[data.length - 1];
    const doc = parser.parseFromString(Object(lastData)["html-content"], "text/html");

    doc.documentElement.style.height = "100%";
    doc.body.style.height = "95%";

    const firstDiv = doc.body.querySelector("div");
    if (firstDiv) firstDiv.style.height = "100%";

    const serializer = new XMLSerializer();
    const newHtmlString = serializer.serializeToString(doc);

    // Get the current content of the iframe
    const iframe = document.getElementById('html-content') as HTMLIFrameElement;
    console.log("3.5")
    if (iframe && iframe.contentDocument) {
      console.log("44")
      const currentContent = iframe.contentDocument.documentElement.outerHTML;

      // Only update if the new content is different
      if (currentContent !== newHtmlString) {
        console.log("55")
        iframe.contentDocument.open();
        iframe.contentDocument.write(newHtmlString);
        iframe.contentDocument.close();
      }
    }
  }
}