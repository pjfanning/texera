import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { PanelService } from "../../service/panel/panel.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { WorkflowActionService } from "../../service/workflow-graph/model/workflow-action.service"; // Import the WorkflowActionService
import mapping from "../../../../assets/migration_tool/mapping";

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
  private socket: WebSocket | null = null; // WebSocket connection

  constructor(private panelService: PanelService, private workflowActionService: WorkflowActionService) {} // Inject WorkflowActionService

  ngOnInit(): void {
    // Listen for messages from the Jupyter notebook iframe
    window.addEventListener("message", this.handleMessage);

    // Subscribe to the PanelService to control the visibility of the panel
    this.panelService.jupyterNotebookPanelVisible$
      .pipe(takeUntil(this.destroy$)) // Unsubscribe when destroy$ emits
      .subscribe((visible: boolean) => {
        this.isVisible = visible;
      });

    // Initialize WebSocket connection (optional)
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    window.removeEventListener("message", this.handleMessage);

    // Emit a value to signal that all subscriptions should be unsubscribed
    this.destroy$.next();
    this.destroy$.complete(); // Close the subject to prevent memory leaks

    // Close WebSocket connection
    if (this.socket) {
      this.socket.close();
    }
  }

  // Handle incoming messages from the notebook
  handleMessage = (event: MessageEvent) => {
    console.log("Received message from origin:", event.origin);

    const allowedOrigins = ["http://localhost:4200", "http://localhost:8888"];
    if (!allowedOrigins.includes(event.origin)) {
      console.log("Invalid origin:", event.origin);
      return;
    }

    const { action, cellIndex, cellContent, cellUUID } = event.data;
    if (action === "cellClicked") {
      this.highlightedCell = cellIndex;
      this.cellContent[cellIndex] = cellContent || `Cell ${cellIndex + 1}`;
      console.log(`highlighted cell: ${this.highlightedCell}`);
      console.log(`highlighted cell content: ${cellContent || `Cell ${cellIndex + 1}`}`);

      // Get the corresponding Texera components from the mapping based on the cell UUID
      // @ts-ignore
      const components = mapping.cell_to_operator[cellUUID] || [];

      // Unhighlight all previous highlights before highlighting the relevant components
      this.workflowActionService.unhighlightOperators(...this.workflowActionService.getTexeraGraph().getAllOperators().map(op => op.operatorID));

      if (components.length > 0) {
        console.log(`Corresponding components for cell ${cellIndex + 1} (UUID: ${cellUUID}):`, components);
        // Highlight the corresponding Texera components
        this.workflowActionService.highlightOperators(false, ...components);
      } else {
        console.log(`No corresponding components found for cell ${cellIndex + 1} (UUID: ${cellUUID}).`);
      }
    }
  };

  // Trigger a click inside the Jupyter iframe to highlight a cell
  triggerCellClickInsideIframe(cellIndex: number) {
    const iframe = this.iframeRef.nativeElement;
    if (iframe && iframe.contentWindow) {
      iframe.contentWindow.postMessage({ action: "triggerCellClick", cellIndex }, "http://localhost:8888");
    }
  }

  // Close the panel
  closePanel() {
    this.panelService.closeJupyterNotebookPanel();
  }

  // Handle workflow component clicks
  onWorkflowComponentClick(operatorID: string) {
    // Find the corresponding Jupyter cell(s) using the mapping.json
    // @ts-ignore
    const correspondingCells = mapping.operator_to_cell[operatorID] || [];
    correspondingCells.forEach((cellID: string) => {
      const cellIndex = this.findCellIndex(cellID);
      if (cellIndex !== null) {
        this.highlightCell(cellIndex); // Highlight the corresponding cell
      }
    });
  }

  // Function to find the index of a cell by its cellID from notebook_with_ids
  findCellIndex(cellID: string): number | null {
    // Logic to find the index of a cell from notebook_with_ids.ipynb
    return this.cellContent.findIndex((content) => content.includes(cellID));
  }

  // Function to highlight a cell in the Jupyter notebook
  highlightCell(cellIndex: number) {
    this.triggerCellClickInsideIframe(cellIndex); // Use postMessage to highlight the cell in the notebook
  }

  // Optional: WebSocket connection initialization
  connectWebSocket() {
    this.socket = new WebSocket("ws://localhost:8888");
    this.socket.onopen = () => console.log("WebSocket connection established");
    this.socket.onmessage = (event) => console.log("Message from WebSocket:", JSON.parse(event.data));
    this.socket.onclose = () => console.log("WebSocket connection closed");
    this.socket.onerror = (error) => console.error("WebSocket error:", error);
  }
}
