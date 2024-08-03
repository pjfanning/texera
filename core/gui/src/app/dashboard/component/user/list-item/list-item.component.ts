import { Component, Input, Output, OnInit } from "@angular/core";
import { UntilDestroy, untilDestroyed } from "@ngneat/until-destroy";
import { DashboardEntry } from "src/app/dashboard/type/dashboard-entry";

@UntilDestroy()
@Component({
  selector: 'texera-list-item',
  templateUrl: './list-item.component.html',
  styleUrls: ['./list-item.component.scss'],
})
export class ListItemComponent implements OnInit{
  ROUTER_WORKFLOW_BASE_URL = "/dashboard/workspace";
  ROUTER_USER_PROJECT_BASE_URL = "/dashboard/user-project";
  ROUTER_DATASET_BASE_URL = "/dashboard/dataset";
  public entryLink: string = "";
  public iconType: string = "";
  private _entry?: DashboardEntry;
  @Input()
  get entry(): DashboardEntry {
    if (!this._entry) {
      throw new Error("entry property must be provided.");
    }
    return this._entry;
  }
  set entry(value: DashboardEntry) {
    this._entry = value;
  }
  ngOnInit(): void {
      if (this.entry.type === "workflow") {
        this.entryLink = this.ROUTER_WORKFLOW_BASE_URL + "/" + this.entry.id;
        this.iconType = "project";
      }
      else if (this.entry.type === "project") {
        this.entryLink = this.ROUTER_USER_PROJECT_BASE_URL + '/' + this.entry.id;
        this.iconType = "container";
      }
      else if (this.entry.type === "dataset") {
        this.entryLink = this.ROUTER_DATASET_BASE_URL + '/' + this.entry.id;
        this.iconType = "database";
      }
      else if (this.entry.type === "file") {
        // not sure where to redirect
        this.iconType = "folder-open";
      }
      else {
        throw new Error("Unexpected type in DashboardEntry.");
      }
  }
}
