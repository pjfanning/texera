import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { PanelService } from "../../service/panel/panel.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

@Component({
  selector: "texera-jupyter-notebook-panel",
  templateUrl: "./jupyter-notebook-panel.component.html",
  styleUrls: ["./jupyter-notebook-panel.component.scss"]
})
export class JupyterNotebookPanelComponent implements OnInit, OnDestroy {
  @ViewChild("iframeRef", { static: true }) iframeRef!: ElementRef<HTMLIFrameElement>;

  cellContent: string[] = [];
  highlightedCell: number | null = null;
  isVisible: boolean = true; // Tracks panel visibility
  private destroy$ = new Subject<void>(); // Used to signal when to unsubscribe

  constructor(private panelService: PanelService) {}

  ngOnInit(): void {
    // Listen for messages from the Jupyter notebook
    window.addEventListener("message", this.handleMessage);

    // Subscribe to the PanelService to control the visibility of the panel
    this.panelService.jupyterNotebookPanelVisible$
      .pipe(takeUntil(this.destroy$)) // Unsubscribe when destroy$ emits
      .subscribe((visible: boolean) => {
        this.isVisible = visible;
      });
  }

  ngOnDestroy(): void {
    window.removeEventListener("message", this.handleMessage);

    // Emit a value to signal that all subscriptions should be unsubscribed
    this.destroy$.next();
    this.destroy$.complete(); // Close the subject to prevent memory leaks
  }

  handleMessage = (event: MessageEvent) => {
    console.log("Received message from origin:", event.origin);
    if (event.origin !== "http://localhost:8888") {
      console.log("Invalid origin:", event.origin);
      return;
    }

    const { action, cellIndex, cellContent } = event.data;
    if (action === "cellClicked") {
      this.highlightedCell = cellIndex;
      this.cellContent[cellIndex] = cellContent || `Cell ${cellIndex + 1}`;
    }
  };

  triggerCellClickInsideIframe(cellIndex: number) {
    const iframe = this.iframeRef.nativeElement;
    if (iframe && iframe.contentWindow) {
      iframe.contentWindow.postMessage({ action: "triggerCellClick", cellIndex }, "http://localhost:8888");
    }
  }

  closePanel() {
    this.panelService.closeJupyterNotebookPanel(); // Hide the panel using the PanelService
  }
}
