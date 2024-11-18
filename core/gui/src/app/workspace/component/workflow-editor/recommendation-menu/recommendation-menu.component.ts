import { Component, Input } from '@angular/core';
import {WorkflowVersionService} from "../../../../dashboard/service/user/workflow-version/workflow-version.service";
import {untilDestroyed} from "@ngneat/until-destroy";

@Component({
  selector: 'texera-operator-recommendation-menu',
  templateUrl: './recommendation-menu.component.html',
  styleUrls: ['./recommendation-menu.component.scss'],
})
export class RecommendationMenuComponent {
  @Input() tips: string[] = [];
  selectedRecommendationIndex: number | null = null;

  constructor(
    public workflowVersionService: WorkflowVersionService,
  ) {
  }

  onRecommendedOperationClick(index: number): void {
    this.selectedRecommendationIndex = index;

    // this.workflowVersionService
    //   .retrieveWorkflowByVersion(<number>this.workflowActionService.getWorkflowMetadata()?.wid, vid)
    //   .pipe(untilDestroyed(this))
    //   .subscribe(workflow => {
    //     this.workflowVersionService.displayParticularVersion(workflow, vid, displayedVersionId);
    //   });
  }

  onConfirmClick(): void {
    if (this.selectedRecommendationIndex !== null) {
      const selectedTip = this.tips[this.selectedRecommendationIndex];
      console.log(`Confirmed tip: ${selectedTip}`);
      // Handle the confirmed tip action
    }
  }
}
