import { Component, Input, OnChanges, OnInit, SimpleChanges } from "@angular/core";
import { ExecuteWorkflowService } from "../../../service/execute-workflow/execute-workflow.service";
import { BreakpointTriggerInfo } from "../../../types/workflow-common.interface";
import { ExecutionState } from "src/app/workspace/types/execute-workflow.interface";
import { WorkflowConsoleService } from "../../../service/workflow-console/workflow-console.service";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";

@UntilDestroy()
@Component({
  selector: "texera-console-frame",
  templateUrl: "./console-frame.component.html",
  styleUrls: ["./console-frame.component.scss"],
})
export class ConsoleFrameComponent implements OnInit, OnChanges {
  @Input() operatorId?: string;
  // display error message:
  errorMessages: ReadonlyArray<string> = [];

  // display print
  consoleMessages: ReadonlyArray<string> = [];

  constructor(
    private executeWorkflowService: ExecuteWorkflowService,
    private workflowConsoleService: WorkflowConsoleService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.operatorId = changes.operatorId?.currentValue;
    this.renderConsole();
  }

  ngOnInit(): void {
    // make sure the console is re-rendered upon state changes
    this.registerAutoConsoleRerender();
  }

  registerAutoConsoleRerender() {
    this.executeWorkflowService
      .getExecutionStateStream()
      .pipe(untilDestroyed(this))
      .subscribe(event => {
        if (
          event.previous.state === ExecutionState.BreakpointTriggered &&
          event.current.state === ExecutionState.Completed
        ) {
          // intentionally do nothing to leave the information displayed as it is
          // when kill a workflow after hitting breakpoint
        } else if (
          event.previous.state === ExecutionState.Initializing &&
          event.current.state === ExecutionState.Running
        ) {
          // clear the console for the next execution
          this.clearConsole();
        } else {
          // re-render the console, this may update the console with error messages or console messages
          this.renderConsole();
        }
      });

    this.workflowConsoleService
      .getConsoleMessageUpdateStream()
      .pipe(untilDestroyed(this))
      .subscribe(_ => this.renderConsole());
  }

  clearConsole() {
    this.consoleMessages = [];
    this.errorMessages = [];
  }

  renderConsole() {
    // try to fetch if we have breakpoint info
    const breakpointTriggerInfo = this.executeWorkflowService.getBreakpointTriggerInfo();

    if (this.operatorId) {
      // first display error messages if applicable
      if (this.operatorId === breakpointTriggerInfo?.operatorId) {
        // if we hit a breakpoint
        this.displayBreakpoint(breakpointTriggerInfo);
      } else {
        // otherwise we assume it's a fault
        this.displayFault();
      }

      // always display console messages
      this.displayConsoleMessages(this.operatorId);
    }
  }

  displayBreakpoint(breakpointTriggerInfo: BreakpointTriggerInfo) {
    this.errorMessages = [...this.errorMessages, breakpointTriggerInfo.breakpoint.message];

  }

  displayFault() {
    this.errorMessages = this.executeWorkflowService.getErrorMessages();
  }

  displayConsoleMessages(operatorId: string) {
    this.consoleMessages = operatorId ? this.workflowConsoleService.getConsoleMessages(operatorId) || [] : [];
  }
}
