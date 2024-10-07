import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";

@Component({
  selector: "texera-jupyter-notebook-panel",
  templateUrl: "./jupyter-notebook-panel.component.html",
  styleUrls: ["./jupyter-notebook-panel.component.scss"]
})
export class JupyterNotebookPanelComponent implements OnInit, OnDestroy {
  @ViewChild("iframeRef", { static: true }) iframeRef!: ElementRef<HTMLIFrameElement>;

  cellContent: string[] = [];
  highlightedCell: number | null = null;
  isVisible: boolean = true; // Controls panel visibility

  constructor() {}

  ngOnInit(): void {
    window.addEventListener("message", this.handleMessage);
  }

  ngOnDestroy(): void {
    window.removeEventListener("message", this.handleMessage);
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
    this.isVisible = false; // Hide the panel when close button is clicked
  }
}
