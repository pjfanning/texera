import { Component, Input } from "@angular/core";
import { HubWorkflow} from "../../type/hub-workflow.interface";

@Component({
  selector: "texera-hub-result-card",
  templateUrl: "./hub-result-card.component.html",
  styleUrls: ["./hub-result-card.component.scss"]
})
export class HubResultCardComponent {
  @Input() workflow: HubWorkflow = {
    name: "Unnamed Workflow",
    description: undefined,
    wid: undefined,
    content: "", // 需要根据 WorkflowContent 的结构调整默认值
    creationTime: undefined,
    lastModifiedTime: undefined
  };

  truncatedDescription: string | undefined;
  fullDescription: string | undefined;
  showFullDescription: boolean = false;

  ngOnInit(): void {
    this.truncatedDescription = this.getDescription(this.workflow.description || "", 40);
    this.fullDescription = this.getDescription(this.workflow.description || "", 100);
  }

  getDescription(description: string, maxWords: number): string {
    const words = description.split(" ");
    if (words.length > maxWords) {
      return words.slice(0, maxWords).join(" ") + "...";
    }
    return description;
  }

  onMouseEnter(): void {
    this.showFullDescription = true;
  }

  onMouseLeave(): void {
    this.showFullDescription = false;
  }
}
