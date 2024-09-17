import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import html2canvas from "html2canvas";
import { Observable, Observer, of, firstValueFrom } from "rxjs";
import { map, catchError, tap } from "rxjs/operators";
import { WorkflowActionService } from "../workflow-graph/model/workflow-action.service";
import { WorkflowResultService } from "../workflow-result/workflow-result.service";
import { NotificationService } from "src/app/common/service/notification/notification.service";
import { ReportAiAssistantService } from "./report-ai-assistant.service";
import { AppSettings } from "src/app/common/app-setting";

const AI_ASSISTANT_API_BASE_URL = `${AppSettings.getApiEndpoint()}`;

@Injectable({
  providedIn: "root",
})
export class ReportGenerationService {
  private isAIAssistantEnabled: boolean | null = null;
  constructor(
    private http: HttpClient,
    public workflowActionService: WorkflowActionService,
    private workflowResultService: WorkflowResultService,
    private notificationService: NotificationService,
    private reportAiAssistantService: ReportAiAssistantService,
  ) {}

  /**
   * Captures a snapshot of the workflow editor and returns it as a base64-encoded PNG image URL.
   * @param {string} workflowName - The name of the workflow.
   * @returns {Observable<string>} An observable that emits the base64-encoded PNG image URL of the workflow snapshot.
   */
  public generateWorkflowSnapshot(workflowName: string): Observable<string> {
    return new Observable((observer: Observer<string>) => {
      const element = document.querySelector("#workflow-editor") as HTMLElement;
      if (element) {
        // Ensure all resources are loaded
        const promises: Promise<void>[] = [];
        const images = element.querySelectorAll("image");

        // Create promises for each image to ensure they are loaded
        images.forEach(img => {
          const imgSrc = img.getAttribute("xlink:href");
          if (imgSrc) {
            promises.push(
              new Promise((resolve, reject) => {
                const imgElement = new Image();
                imgElement.src = imgSrc;
                imgElement.onload = () => resolve();
                imgElement.onerror = () => reject();
              })
            );
          }
        });

        // Wait for all images to load
        Promise.all(promises)
          .then(() => {
            // Capture the snapshot using html2canvas
            return html2canvas(element, {
              logging: true,
              useCORS: true,
              allowTaint: true,
              foreignObjectRendering: true,
            });
          })
          .then((canvas: HTMLCanvasElement) => {
            const dataUrl: string = canvas.toDataURL("image/png");
            observer.next(dataUrl);
            observer.complete();
          })
          .catch((error: any) => {
            observer.error(error);
          });
      } else {
        observer.error("Workflow editor element not found");
      }
    });
  }

  /**
   * Retrieves and processes results for all specified operators within the workflow.
   * * This function iterates over each operator ID, fetches the corresponding result details via `retrieveOperatorInfoReport`,
   * and collects these results into an array. The function returns an observable that emits the processed results,
   * which can be used to generate a comprehensive HTML report or for further processing.
   *
   * @param {string[]} operatorIds - An array of operator IDs representing each operator in the workflow.
   * @returns {Observable<{operatorId: string, html: string}[]>} - An observable that emits an array of objects,
   * each containing an `operatorId` and its corresponding HTML representation of the result.
   * This result array can be used to generate an HTML report or for other purposes.
   */
  public getAllOperatorResults(operatorIds: string[]): Observable<{ operatorId: string; html: string }[]> {
    return new Observable(observer => {
      const allResults: { operatorId: string; html: string }[] = [];
      const promises = operatorIds.map(operatorId => {
        return this.retrieveOperatorInfoReport(operatorId, allResults);
      });

      Promise.all(promises)
        .then(() => {
          observer.next(allResults);
          observer.complete();
        })
        .catch(error => {
          observer.error("Error in retrieving operator results: " + error.message);
        });
    });
  }

  /**
   * Retrieves and processes results for all specified operators within the workflow.
   * This function iterates over each operator ID, fetches the corresponding result details via `retrieveOperatorInfoReport`,
   * and collects these results into an array. The function returns an observable that emits the processed results,
   * which can be used to generate a comprehensive HTML report or for further processing.
   *
   * @param operatorId
   * @param allResults
   * @returns {Promise<void>}
   */
  public retrieveOperatorInfoReport(
    operatorId: string,
    allResults: { operatorId: string; html: string }[]
  ): Observable<void> {
    return new Observable<void>(observer => {
      this.reportAiAssistantService.checkAIAssistantEnabled().subscribe((AIEnabled: boolean) => {
        console.log(`AI Assistant Enabled: ${AIEnabled}`);
        console.log(`Processing ${operatorId}`);
        try {
          // Retrieve the result service and paginated result service for the operator
          const resultService = this.workflowResultService.getResultService(operatorId);
          const paginatedResultService = this.workflowResultService.getPaginatedResultService(operatorId);

          const workflowContent = this.workflowActionService.getWorkflowContent();
          const operatorDetails = workflowContent.operators.find(op => op.operatorID === operatorId);

          const operatorDetailsHtml = `
            <div style="text-align: center;">
              <h4>Operator Details</h4>
              <div id="json-editor-${operatorId}" style="height: 400px;"></div>
              <script>
                document.addEventListener('DOMContentLoaded', function() {
                  const container = document.querySelector("#json-editor-${operatorId}");
                  const options = { mode: 'view', language: 'en' };
                  const editor = new JSONEditor(container, options);
                  editor.set(${JSON.stringify(operatorDetails)});
                });
              </script>
            </div>`;

          // Generate the comment and then continue with the rest of the logic
          this.reportAiAssistantService.generateComment(operatorDetails).subscribe(comment => {
            if (paginatedResultService) {
              paginatedResultService.selectPage(1, 10).subscribe({
                next: pageData => {
                  try {
                    // Handle the paginated results
                    const table = pageData.table;

                    if (!table.length) {
                      allResults.push({
                        operatorId,
                        html: `
                        <h3>Operator ID: ${operatorId}</h3>
                        <p>No results found for operator</p>
                        <button onclick="toggleDetails('details-${operatorId}')">Toggle Details</button>
                        <div id="details-${operatorId}" style="display: none;">${operatorDetailsHtml}</div>
                        <div contenteditable="true" id="comment-${operatorId}" style="width: 100%; margin-top: 10px; border: 1px solid black; padding: 10px;">${comment}</div>`,
                      });
                      observer.next();
                      observer.complete();
                      return;
                    }

                    // Generate an HTML table to display the results
                    const columns: string[] = Object.keys(table[0]);
                    const rows: any[][] = table.map(row => columns.map(col => row[col]));

                    const htmlContent: string = `
                    <div style="width: 50%; margin: 0 auto; text-align: center;">
                      <h3>Operator ID: ${operatorId}</h3>
                      <table style="width: 100%; border-collapse: collapse; margin: 0 auto;">
                        <thead>
                          <tr>${columns
                      .map(
                        col =>
                          `<th style="border: 1px solid black; padding: 8px; text-align: center;">${col}</th>`
                      )
                      .join("")}</tr>
                        </thead>
                        <tbody>
                          ${rows
                      .map(
                        row =>
                          `<tr>${row
                            .map(
                              cell =>
                                `<td style="border: 1px solid black; padding: 8px; text-align: center;">${String(cell)}</td>`
                            )
                            .join("")}</tr>`
                      )
                      .join("")}
                        </tbody>
                      </table>
                      <button onclick="toggleDetails('details-${operatorId}')">Toggle Details</button>
                      <div id="details-${operatorId}" style="display: none;">${operatorDetailsHtml}</div>
                      <div contenteditable="true" id="comment-${operatorId}" style="width: 100%; margin-top: 10px; border: 1px solid black; padding: 10px;">${comment}</div>
                    </div>`;

                    allResults.push({ operatorId, html: htmlContent });
                    observer.next();
                    observer.complete();
                  } catch (error) {
                    const errorMessage = (error as Error).message || "Unknown error";
                    this.notificationService.error(
                      `Error processing results for operator ${operatorId}: ${errorMessage}`
                    );
                    observer.error(error);
                  }
                },
                error: error => {
                  const errorMessage = (error as Error).message || "Unknown error";
                  this.notificationService.error(
                    `Error retrieving paginated results for operator ${operatorId}: ${errorMessage}`
                  );
                  observer.error(error);
                },
              });
            } else if (resultService) {
              const data = resultService.getCurrentResultSnapshot();

              if (data) {
                const parser = new DOMParser();
                const lastData = data[data.length - 1];
                const doc = parser.parseFromString(Object(lastData)["html-content"], "text/html");

                doc.documentElement.style.height = "50%";
                doc.body.style.height = "50%";

                const firstDiv = doc.body.querySelector("div");
                if (firstDiv) firstDiv.style.height = "100%";

                const serializer = new XMLSerializer();
                const newHtmlString = serializer.serializeToString(doc);

                const visualizationHtml = `
                <h3 style="text-align: center;">Operator ID: ${operatorId}</h3>
                ${newHtmlString}
                <button onclick="toggleDetails('details-${operatorId}')">Toggle Details</button>
                <div id="details-${operatorId}" style="display: none;">${operatorDetailsHtml}</div>
                <div contenteditable="true" id="comment-${operatorId}" style="width: 100%; margin-top: 10px; border: 1px solid black; padding: 10px;">${comment}</div>`;

                allResults.push({ operatorId, html: visualizationHtml });
                observer.next();
                observer.complete();
              } else {
                allResults.push({
                  operatorId,
                  html: `
                  <h3>Operator ID: ${operatorId}</h3>
                  <p>No data found for operator</p>
                  <button onclick="toggleDetails('details-${operatorId}')">Toggle Details</button>
                  <div id="details-${operatorId}" style="display: none;">${operatorDetailsHtml}</div>
                  <div contenteditable="true" id="comment-${operatorId}" style="width: 100%; margin-top: 10px; border: 1px solid black; padding: 10px;">${comment}</div>`,
                });
                observer.next();
                observer.complete();
              }
            } else {
              allResults.push({
                operatorId,
                html: `
                <h3>Operator ID: ${operatorId}</h3>
                <p>No results found for operator</p>
                <button onclick="toggleDetails('details-${operatorId}')">Toggle Details</button>
                <div id="details-${operatorId}" style="display: none;">${operatorDetailsHtml}</div>
                <div contenteditable="true" id="comment-${operatorId}" style="width: 100%; margin-top: 10px; border: 1px solid black; padding: 10px;">${comment}</div>`,
              });
              observer.next();
              observer.complete();
            }
          })
        } catch (error) {
          const errorMessage = (error as Error).message || "Unknown error";
          this.notificationService.error(
            `Unexpected error in retrieveOperatorInfoReport for operator ${operatorId}: ${errorMessage}`
          );
          observer.error(error);
        }
      });
    });
  }


  /**
   * Generates an HTML report containing the workflow snapshot and all operator results, and triggers a download of the report.
   *
   * @param {string} workflowSnapshot - The base64-encoded PNG image URL of the workflow snapshot.
   * @param {string[]} allResults - An array of HTML strings representing the results of each operator.
   * @param {string} workflowName - The name of the workflow, used for naming the final report.
   * @returns {void}
   */
  public generateReportAsHtml(
    workflowSnapshot: string,
    allResults: string[],
    workflowName: string
  ): Observable<void> {
    const workflowContent = this.workflowActionService.getWorkflowContent();
    let finalComment: string = "";

    // Assuming operatorDetails is defined earlier in the code and contains necessary details
    return new Observable<void>(observer => {
      this.reportAiAssistantService.generateSummaryComment(workflowContent)
        .subscribe({
          next: (comment) => {
            finalComment = comment;

            // HTML content generation with final comment
            const htmlContent = `
              <html>
                <head>
                  <title>Operator Results</title>
                  <!-- Link to JSONEditor CSS file-->
                  <link href="https://cdn.jsdelivr.net/npm/jsoneditor@10.1.0/dist/jsoneditor.min.css" rel="stylesheet" type="text/css" />
                  <script src="https://cdn.jsdelivr.net/npm/jsoneditor@10.1.0/dist/jsoneditor.min.js"></script>
                  <style>
                    .button {
                      margin-top: 20px;
                      padding: 10px 20px;
                      border: 1px solid #ccc;
                      background-color: #f8f8f8;
                      color: #333;
                      border-radius: 5px;
                      cursor: pointer;
                      font-size: 14px;
                    }
                    .button:hover {
                      background-color: #e8e8e8;
                    }
                    .hidden-input {
                      display: none;
                    }
                    .json-editor-container {
                      height: 400px;
                    }
                    .comment-box {
                      margin-top: 20px;
                      padding: 10px;
                      border: 1px solid #ccc;
                      border-radius: 5px;
                    }
                    .editable-comment-box {
                      width: 100%;
                      margin-top: 10px;
                      border: 1px solid black;
                      padding: 10px;
                      text-align: left;
                    }
                  </style>
                  <script>
                    function toggleDetails(id) {
                      const detailsElement = document.getElementById(id);
                      if (detailsElement.style.display === "none") {
                        detailsElement.style.display = "block";
                      } else {
                        detailsElement.style.display = "none";
                      }
                    }

                    function downloadJson() {
                      const workflowContent = ${JSON.stringify(this.workflowActionService.getWorkflowContent())};
                      const jsonBlob = new Blob([JSON.stringify(workflowContent, null, 2)], {type: "application/json"});
                      const url = URL.createObjectURL(jsonBlob);
                      const a = document.createElement("a");
                      a.href = url;
                      a.download = "${workflowName}-workflow.json";
                      a.click();
                      URL.revokeObjectURL(url);
                    }
                  </script>
                </head>
                <body>
                  <div style="text-align: center;">
                    <h2>${workflowName} Static State</h2>
                    <img src="${workflowSnapshot}" alt="Workflow Snapshot" style="width: 100%; max-width: 800px;">
                  </div>
                  ${allResults.join("")}
                  <div style="text-align: center; margin-top: 20px;">
                    <div class="comment-box">
                      <h3>Summary</h3>
                      <div contenteditable="true" id="comment-summary" class="editable-comment-box">${finalComment}</div>
                    </div>
                    <button class="button" onclick="downloadJson()">Download Workflow JSON</button>
                  </div>
                </body>
              </html>`;

            // Create and download the HTML report
            const blob = new Blob([htmlContent], { type: "text/html" });
            const url = URL.createObjectURL(blob);
            const fileName = `${workflowName}-report.html`;
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            a.click();
            URL.revokeObjectURL(url);

            observer.next();
            observer.complete();
          },
          error: (error: unknown) => {
            const errorMessage = error instanceof Error ? error.message : "Unknown error";
            console.error(`Error generating summary comment: ${errorMessage}`);
            observer.error(error);
          }
        });
    });
  }

}
