import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class PanelService {
  private jupyterNotebookPanelVisible = new BehaviorSubject<boolean>(false);
  public jupyterNotebookPanelVisible$ = this.jupyterNotebookPanelVisible.asObservable();

  openPanel(panelName: string): void {
    if (panelName === "JupyterNotebookPanel") {
      this.jupyterNotebookPanelVisible.next(true);
    }
  }

  closeJupyterNotebookPanel(): void {
    this.jupyterNotebookPanelVisible.next(false);
  }
}
