import { Component, Input } from '@angular/core';
import {WorkflowVersionService} from "../../../../dashboard/service/user/workflow-version/workflow-version.service";
import {UntilDestroy, untilDestroyed} from "@ngneat/until-destroy";
import {WorkflowActionService} from "../../../service/workflow-graph/model/workflow-action.service";
import {WorkflowMetadata} from "../../../../dashboard/type/workflow-metadata.interface";
import {WorkflowContent} from "../../../../common/type/workflow";
import {WorkflowEdition} from "../../../types/workflow-editing.interface";
import {WorkflowEditingService} from "../../../service/edit-workflow/workflow-editing.service";
import {NotificationService} from "../../../../common/service/notification/notification.service";
import {BehaviorSubject, Observable} from "rxjs";

@UntilDestroy()
@Component({
  selector: 'texera-operator-recommendation-menu',
  templateUrl: './recommendation-menu.component.html',
  styleUrls: ['./recommendation-menu.component.scss'],
})
export class RecommendationMenuComponent {
  @Input() editions: WorkflowEdition[] = [];
  selectedRecommendedEditionIndex: number | null = null;

  constructor(
    public workflowVersionService: WorkflowVersionService,
    public workflowActionService: WorkflowActionService,
    public workflowEditingService: WorkflowEditingService,
    public notificationService: NotificationService
  ) {
  }

  onRecommendedOperationClick(index: number): void {
    this.selectedRecommendedEditionIndex = index;

    const workflowEdition: WorkflowEdition = this.editions[this.selectedRecommendedEditionIndex];
    this.workflowEditingService
      .applyWorkflowEdition(workflowEdition)
      .pipe(untilDestroyed(this))
      .subscribe({
        next: workflowContentWithEdition => {
          this.workflowEditingService.displayRecommendedWorkflowPreview(workflowEdition, workflowContentWithEdition);
        },
        error: err => {
          this.notificationService.error(err)
        }
      })
  }
}
