import {Component, Input, OnInit} from "@angular/core";
import { interval } from "rxjs";
import { switchMap } from "rxjs/operators";
import { WorkflowComputingUnitManagingService } from "../../service/workflow-computing-unit/workflow-computing-unit-managing.service";
import { DashboardWorkflowComputingUnit } from "../../types/workflow-computing-unit";
import {NotificationService} from "../../../common/service/notification/notification.service";

@Component({
  selector: "texera-computing-unit-selection",
  templateUrl: "./computing-unit-selection.component.html",
  styleUrls: ["./computing-unit-selection.component.scss"],
})
export class ComputingUnitSelectionComponent implements OnInit {
  @Input()
  workflowId: number | undefined;


  selectedComputingUnitUri: string | null = null;
  computingUnits: DashboardWorkflowComputingUnit[] = [];
  private readonly REFRESH_INTERVAL_MS = 2000;

  constructor(
    private computingUnitService: WorkflowComputingUnitManagingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.refreshComputingUnits();
  }

  /**
   * Periodically refresh the list of computing units.
   */
  private refreshComputingUnits(): void {
    interval(this.REFRESH_INTERVAL_MS)
      .pipe(
        switchMap(() => this.computingUnitService.listComputingUnits())
      )
      .subscribe({
        next: (units) => this.updateComputingUnits(units),
        error: (err) =>
          console.error("Failed to fetch computing units:", err),
      });
  }

  /**
   * Update the computing units list, maintaining object references for the same CUID.
   */
  private updateComputingUnits(newUnits: DashboardWorkflowComputingUnit[]): void {
    console.log("update using: ", newUnits)
    const unitMap = new Map(
      this.computingUnits.map((unit) => [unit.computingUnit.cuid, unit])
    );

    this.computingUnits = newUnits.map((newUnit) =>
      unitMap.has(newUnit.computingUnit.cuid)
        ? Object.assign(unitMap.get(newUnit.computingUnit.cuid)!, newUnit)
        : newUnit
    );
  }

  /**
   * Start a new computing unit.
   */
  startComputingUnit(): void {
    const computeUnitName = `Compute for Workflow ${this.workflowId}`
    this.computingUnitService
      .createComputingUnit(`Compute for Workflow ${this.workflowId}`)
      .subscribe({
        next: (unit) => {
          this.notificationService.success(`Successfully create the new compute unit`)
          this.refreshComputingUnits();
        },
        error: (err) => this.notificationService.error("Failed to start computing unit:", err),
      });
  }

  /**
   * Terminate a computing unit.
   * @param cuid The CUID of the unit to terminate.
   */
  terminateComputingUnit(cuid: number): void {
    const uri = this.computingUnits.find(
      (unit) => unit.computingUnit.cuid === cuid
    )?.uri;

    if (!uri) {
      this.notificationService.error("Invalid computing unit URI.");
      return;
    }

    this.computingUnitService.terminateComputingUnit(uri).subscribe({
      next: () => {
        this.notificationService.success(`Terminated computing unit with URI: ${uri}`);
        this.refreshComputingUnits();
      },
      error: (err) =>
        this.notificationService.error("Failed to terminate computing unit:", err),
    });
  }

  /**
   * Get badge color based on the unit's status.
   */
  getBadgeColor(status: string): string {
    return status === "Running" ? "green" : "yellow";
  }
}
