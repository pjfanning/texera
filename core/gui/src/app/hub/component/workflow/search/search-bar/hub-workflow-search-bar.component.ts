import { Component } from "@angular/core";

@Component({
  selector: "texera-hub-workflow-search-bar",
  templateUrl: "hub-workflow-search-bar.component.html",
  styleUrls: ["hub-workflow-search-bar.component.scss"],
})

export class HubWorkflowSearchBarComponent {
  inputValue?: string;
  options: string[] = [];

  onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.options = value ? [value, value + value, value + value + value] : [];
  }
}