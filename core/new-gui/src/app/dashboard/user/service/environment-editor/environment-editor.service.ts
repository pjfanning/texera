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
    private workflowEnvironmentService: WorkflowEnvironmentService
  ) {}

  public clickDisplayEnvironmentEditor(wid: number): void {
    // unhighlight all the current highlighted operators/groups/links
    const elements = this.workflowActionService.getJointGraphWrapper().getCurrentHighlights();
    this.workflowActionService.getJointGraphWrapper().unhighlightElements(elements);

    this.workflowEnvironmentService.retrieveEnvironmentOfWorkflow(wid).subscribe(env => {
      if (env && env.environment.eid) {
        const eid = env.environment.eid;
        this.environmentEditorObservable.next([DISPLAY_ENVIRONMENT_EDITOR_EVENT, eid.toString(), wid.toString()]);
        console.log([DISPLAY_ENVIRONMENT_EDITOR_EVENT, eid.toString(), wid.toString()])
      }
    });

  }

  public environmentEditorDisplayObservable(): Observable<readonly string[]> {
    return this.environmentEditorObservable.asObservable();
  }
}
