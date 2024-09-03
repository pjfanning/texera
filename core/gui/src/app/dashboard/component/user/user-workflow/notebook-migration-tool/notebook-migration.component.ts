import { Component, Input } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { JupyterNotebook, JupyterOutput } from "../../../../type/jupyter-notebook.interface";

@Component({
  selector: "texera-jupyter-upload-success",
  templateUrl: "./notebook-migration.component.html",
  styleUrls: ["./notebook-migration.component.scss"],
})
export class JupyterUploadSuccessComponent {
  @Input() notebookContent!: JupyterNotebook;
  datasets: File[] = [];
  popupStage: number = 0; // flag to manage content display
  workflowGeneratingInProgress: boolean = false;
  workflowJsonContent: any; // variable to store the JSON content

  constructor(private http: HttpClient) {}

  getOutputText(output: JupyterOutput): string {
    if (output.output_type === "stream") {
      return output.text ? output.text.join("") : "";
    } else if (output.output_type === "execute_result" || output.output_type === "display_data") {
      return output.data ? output.data["text/plain"] || "" : "";
    } else if (output.output_type === "error") {
      return output.traceback ? output.traceback.join("\n") : "";
    }
    return "";
  }

  onFileChange(event: any): void {
    const files: FileList = event.target.files;
    for (let i = 0; i < files.length; i++) {
      this.datasets.push(files[i]);
    }
  }

  convertToWorkflow(): void {
    this.workflowGeneratingInProgress = true;

    // TODO: replace with non-dummy backend
    setTimeout(() => {
      // eslint-disable-next-line rxjs-angular/prefer-takeuntil
      this.http.get("http://localhost:8000", { responseType: "text" }).subscribe({next: (data) => {
        // Process the data
        this.workflowJsonContent = data;
        this.workflowGeneratingInProgress = false;
        this.popupStage = 1; // Update the flag when conversion is done
      }});
    }, 5000);
  }

  compareOutput(): void {
    console.log("Generating output to compare");
    this.popupStage = 2
    // TODO
  }
}
