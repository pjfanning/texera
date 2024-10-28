import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { PanelService } from "../../service/panel/panel.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

@Component({
  selector: "texera-jupyter-notebook-panel",
  templateUrl: "./thebe-notebook-panel.component.html",
  styleUrls: ["./thebe-notebook-panel.component.scss"]
})
export class ThebeNotebookPanelComponent implements OnInit, OnDestroy {
  isVisible: boolean = false; // Initialize to false, meaning the panel is hidden by default
  private destroy$ = new Subject<void>();

  constructor(private panelService: PanelService) {}

  ngOnInit(): void {
    // Subscribe to the visibility state of the panel
    this.panelService.jupyterNotebookPanelVisible$
      .pipe(takeUntil(this.destroy$))
      .subscribe((visible: boolean) => {
        this.isVisible = visible;

        // if (this.isVisible) {
        //   // The iframe only exists once the panel is visible (because of *ngIf)
        //   this.notebookUrl = "http://localhost:8888/notebooks/work/notebook_with_ids.ipynb?token=mytoken";
        //   this.checkIframeRef();
        // }
      });
  }

  // ngAfterViewInit(): void {
  //   // Ensure iframe is handled after it's available in the DOM
  //   this.checkIframeRef();
  // }

  // checkIframeRef(): void {
  //   setTimeout(() => {
  //     if (this.isVisible && this.iframeRef?.nativeElement) {
  //       console.log("Iframe reference found:", this.iframeRef.nativeElement);
  //       this.panelService.setIframeRef(this.iframeRef.nativeElement);
  //     } else {
  //       console.error("Iframe reference not found yet.");
  //     }
  //   }, 0); // Small timeout to ensure DOM is updated
  // }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete(); // Cleanup subscriptions to avoid memory leaks
  }

  // Close the panel by invoking the service method
  closePanel(): void {
    this.panelService.closeJupyterNotebookPanel();
  }

  toggleHighlight(): void {
    console.log("Highlight me!")
  }
}
