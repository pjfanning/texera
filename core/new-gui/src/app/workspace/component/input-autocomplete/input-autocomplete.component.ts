import { Component } from "@angular/core";
import { FieldType } from "@ngx-formly/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { UserFileService } from "src/app/dashboard/user/service/user-file/user-file.service";
import { FormControl } from "@angular/forms";
import { debounceTime } from "rxjs/operators";
import { map } from "rxjs";
import { EnvironmentService } from "../../../dashboard/user/service/user-environment/environment.service";
import { WorkflowActionService } from "../../service/workflow-graph/model/workflow-action.service";
import { WorkflowPersistService } from "../../../common/service/workflow-persist/workflow-persist.service";

@UntilDestroy()
@Component({
  selector: "texera-input-autocomplete-template",
  templateUrl: "./input-autocomplete.component.html",
  styleUrls: ["input-autocomplete.component.scss"],
})

/* *
 * The FieldType<any> is a workaround for the issue of not assignable FormControl.
 * details https://github.com/ngx-formly/ngx-formly/issues/2842#issuecomment-1066116865
 * need to upgrade formly to v6 to properly fix this issue.
 */
export class InputAutoCompleteComponent extends FieldType<any> {
  // the autocomplete selection list
  public suggestions: string[] = [];

  constructor(
    public environmentService: EnvironmentService,
    public workflowActionService: WorkflowActionService,
    public workflowPersistService: WorkflowPersistService
  ) {
    super();
  }

  // FormControl is a non-nullable and read-only field.
  // This function is used to fit the test cases.
  getControl() {
    if (this.field == undefined) return new FormControl({});
    return this.formControl;
  }

  autocomplete(): void {
    if (this.field.formControl.value === null) {
      this.field.formControl.value = "";
    }
    // currently it's a hard-code UserFileService autocomplete
    // TODO: generalize this callback function with a formly hook.
    const value = this.field.formControl.value.trim();
    const wid = this.workflowActionService.getWorkflowMetadata()?.wid;
    if (wid) {
      // fetch the wid first
      this.workflowPersistService
        .retrieveWorkflowEnvironment(wid)
        .pipe(untilDestroyed(this))
        .subscribe({
          next: env => {
            // then we fetch the file list inorder to do the autocomplete, perform auto-complete based on the current input
            const eid = env.eid;
            if (eid) {
              let query = value;
              if (value.length == 0) {
                query = "";
              }
              this.environmentService
                .getDatasetsFileList(eid, query)
                .pipe(debounceTime(300))
                .pipe(untilDestroyed(this))
                .subscribe(suggestedFiles => {
                  // check if there is a difference between new and previous suggestion
                  const updated =
                    this.suggestions.length != suggestedFiles.length ||
                    this.suggestions.some((e, i) => e !== suggestedFiles[i]);
                  if (updated) this.suggestions = [...suggestedFiles];
                });
            }
          },
        });
    }
  }
}
