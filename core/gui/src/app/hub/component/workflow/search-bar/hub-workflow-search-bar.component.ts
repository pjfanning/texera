import { Component } from "@angular/core";
import { HubWorkflowService } from "../../../service/workflow/hub-workflow.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: "texera-hub-workflow-search-bar",
  templateUrl: "hub-workflow-search-bar.component.html",
})
export class HubWorkflowSearchBarComponent {
  inputValue?: string;
  workflowNames: string[] = [];
  filteredOptions: string[] = [];

  constructor(hubWorkflowService: HubWorkflowService) {
    this.filteredOptions = this.workflowNames;
    hubWorkflowService
      .getWorkflowList()
      .pipe(untilDestroyed(this))
      .subscribe(workflows => (this.workflowNames = workflows.map(obj => obj.name)));
  }

  onChange(value: string) {
    this.filteredOptions = this.workflowNames.filter(
      option => option.toLowerCase().indexOf(value.toLowerCase()) !== -1
    );
  }
}
