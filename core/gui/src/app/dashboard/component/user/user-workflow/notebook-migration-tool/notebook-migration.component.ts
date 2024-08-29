import { Component, Input } from "@angular/core";
import { JupyterNotebook, JupyterOutput } from "../../../../type/jupyter-notebook.interface";

@Component({
  selector: "texera-jupyter-upload-success",
  templateUrl: "./notebook-migration.component.html",
  styleUrls: ["./notebook-migration.component.scss"],
})
export class JupyterUploadSuccessComponent {
  @Input() notebookContent!: JupyterNotebook; // Use non-null assertion operator
  datasets: File[] = [];

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
      if (files[i].type === "text/csv") {
        this.datasets.push(files[i]);
      }
    }
  }

  convertToWorkflow(): void {
    // Implement the logic to convert notebook content and selected datasets into a Texera workflow
    console.log("Converting to Texera Workflow with datasets:", this.datasets);
  }
}
