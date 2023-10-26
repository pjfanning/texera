import {Observable, Subject} from "rxjs";
import {WorkflowActionService} from "../../../../workspace/service/workflow-graph/model/workflow-action.service";
import {Injectable} from "@angular/core";

export const DISPLAY_ENVIRONMENT_EDITOR_EVENT = "display_environment_editor_event";

@Injectable({
  providedIn: "root",
})
export class EnvironmentEditorService {
  private environmentEditorObservable = new Subject<readonly string[]>();

  constructor(
    private workflowActionService: WorkflowActionService,
  ) {}

  public clickDisplayEnvironmentEditor(): void {
    // unhighlight all the current highlighted operators/groups/links
    const elements = this.workflowActionService.getJointGraphWrapper().getCurrentHighlights();
    this.workflowActionService.getJointGraphWrapper().unhighlightElements(elements);

    this.environmentEditorObservable.next([DISPLAY_ENVIRONMENT_EDITOR_EVENT]);
  }

  public environmentEditorDisplayObservable(): Observable<readonly string[]> {
    return this.environmentEditorObservable.asObservable();
  }
}
