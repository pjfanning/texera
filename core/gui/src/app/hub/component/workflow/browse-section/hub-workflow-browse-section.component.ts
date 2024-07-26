import { Component, Input } from "@angular/core";
import { HubWorkflow } from "../../type/hub-workflow.interface";


@Component({
  selector: 'hub-workflow-browse-section',
  templateUrl: './hub-workflow-browse-section.component.html',
  styleUrls: ['./hub-workflow-browse-section.component.scss']
})
export class HubWorkflowBrowseSectionComponent {
@Input() workflowList: HubWorkflow[] = [];
@Input() sectionTitle: String = "";
}
