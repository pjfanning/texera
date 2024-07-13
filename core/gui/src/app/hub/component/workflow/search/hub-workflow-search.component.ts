import { Component } from "@angular/core";

@Component({
  selector: "texera-hub-workflow-search",
  templateUrl: "hub-workflow-search.component.html",
  styleUrls: ["hub-workflow-search.component.scss"],
})

export class HubWorkflowSearchComponent {
  inputValue?: string;
  options: string[] = [];

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.options = value ? [value, value + value, value + value + value] : [];
  }
}