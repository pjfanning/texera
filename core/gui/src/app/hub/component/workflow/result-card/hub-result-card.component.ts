import { Component, Input } from "@angular/core";
import { HubWorkflow} from "../../type/hub-workflow.interface";
import { WorkflowContent } from "../../../../common/type/workflow";

export interface HubWorkflowWithUserInfo extends HubWorkflow{
  name: string;
  description: string | undefined;
  wid: number | undefined;
  creationTime: number | undefined;
  lastModifiedTime: number | undefined;
  userName?: string;
  userGoogleAvatar?: string;
  color?: string;
}

@Component({
  selector: "texera-hub-result-card",
  templateUrl: "./hub-result-card.component.html",
  styleUrls: ["./hub-result-card.component.scss"]
})
export class HubResultCardComponent {
  @Input() workflow!: HubWorkflowWithUserInfo;

  private defaultColors: string[] = ["#66CCFF", "#AEC6CF", "#B2E0C5", "#D3D3D3", "#F5F5DC", "#F8C8DC", "#D8BFD8", "#C3B091"];

  ngOnInit() {
    if (!this.workflow.color) {
      this.workflow.color = this.getRandomColor();
    }
  }

  private getRandomColor(): string {
    const randomIndex = Math.floor(Math.random() * this.defaultColors.length);
    return this.defaultColors[randomIndex];
  }
}
