import {Observable, Subject} from "rxjs";
import {WorkflowActionService} from "../../../../workspace/service/workflow-graph/model/workflow-action.service";
import {Injectable} from "@angular/core";
import {EnvironmentService} from "../user-environment/environment.service";
import {WorkflowEnvironmentService} from "../../../../common/service/workflow-environment/workflow-environment.service";

export const DISPLAY_ENVIRONMENT_EDITOR_EVENT = "display_environment_editor_event";

@Injectable({
  providedIn: "root",
})
export class EnvironmentEditorService {
  private environmentEditorObservable = new Subject<readonly string[]>();

  constructor(
    private workflowActionService: WorkflowActionService,
    private environmentService: EnvironmentService,
  ) {}

  public clickDisplayEnvironmentEditor(wid: number, eid?: number): void {
    // unhighlight all the current highlighted operators/groups/links
    const elements = this.workflowActionService.getJointGraphWrapper().getCurrentHighlights();
    this.workflowActionService.getJointGraphWrapper().unhighlightElements(elements);

    if (eid) {
      // eid is presented, meaning that the environment id is set
      this.environmentEditorObservable.next([DISPLAY_ENVIRONMENT_EDITOR_EVENT, wid.toString(), eid.toString()]);
    } else {
      // eid is not presented, user need to choose one explicitly
      this.environmentEditorObservable.next([DISPLAY_ENVIRONMENT_EDITOR_EVENT, wid.toString()]);
    }
  }

  public environmentEditorDisplayObservable(): Observable<readonly string[]> {
    return this.environmentEditorObservable.asObservable();
  }
}
