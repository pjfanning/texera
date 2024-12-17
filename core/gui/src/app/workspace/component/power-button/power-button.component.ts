import { Component, Input } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { NotificationService } from "../../../common/service/notification/notification.service";
import {
  WorkflowComputingUnitManagingService
} from "../../service/workflow-computing-unit/workflow-computing-unit-managing.service";
 enum PowerState {
  Off = "off",
  Initializing = "initializing",
  Running = "running",
  Stopping = "stopping",
}

@UntilDestroy()
@Component({
  selector: "texera-power-button",
  templateUrl: "./power-button.component.html",
  styleUrls: ["./power-button.component.scss"],
})
export class PowerButtonComponent {
  @Input() uid!: number; // User ID is provided as input
  state: PowerState = PowerState.Off;

  constructor(
    private computingUnitService: WorkflowComputingUnitManagingService,
    private notificationService: NotificationService
  ) {}

  onClick(): void {
    if (this.state === PowerState.Off) {
      this.startComputingUnit();
    } else if (this.state === PowerState.Running) {
      this.terminateComputingUnit();
    }
  }

  private startComputingUnit(): void {
    this.state = PowerState.Initializing;
    const name = `Compute Unit - created by ${this.uid}`;

    this.computingUnitService
      .createComputingUnit(this.uid, name)
      .pipe(untilDestroyed(this))
      .subscribe({
        next: () => {
          this.state = PowerState.Running;
          this.notificationService.success("Compute unit started successfully.");
        },
        error: (err: unknown) => {
          this.state = PowerState.Off;
          this.notificationService.error("Failed to start compute unit.");
          console.error("Error starting compute unit:", err);
        },
      });
  }

  private terminateComputingUnit(): void {
    this.state = PowerState.Stopping;
    const podURI = `urn:kubernetes:default:computing-unit-${this.uid}`;

    this.computingUnitService
      .terminateComputingUnit(podURI)
      .pipe(untilDestroyed(this))
      .subscribe({
        next: () => {
          this.state = PowerState.Off;
          this.notificationService.success("Compute unit terminated successfully.");
        },
        error: (err: unknown) => {
          this.state = PowerState.Running;
          this.notificationService.error("Failed to terminate compute unit.");
          console.error("Error terminating compute unit:", err);
        },
      });
  }

  getButtonText(): string {
    switch (this.state) {
      case PowerState.Off:
        return "Start";
      case PowerState.Initializing:
      case PowerState.Stopping:
        return "Loading...";
      case PowerState.Running:
        return "Running";
    }
  }

  getIconType(): string {
    switch (this.state) {
      case PowerState.Off:
        return "step-forward";
      case PowerState.Initializing:
      case PowerState.Stopping:
        return "loading";
      case PowerState.Running:
        return "pause";
    }
  }

  isRunning(): boolean {
    return this.state === PowerState.Running;
  }
}
