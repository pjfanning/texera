import { Component } from '@angular/core';
import { DashboardEntry } from 'src/app/dashboard/type/dashboard-entry';
import { DashboardProject } from 'src/app/dashboard/type/dashboard-project.interface';

@Component({
  selector: 'texera-general-search-result',
  templateUrl: './general-search-result.component.html',
  styleUrls: ['./general-search-result.component.scss']
})
export class GeneralSearchResultComponent {
  
  sampleProject: DashboardProject = {
    pid: 1,
    name: "test",
    description: "This is a sample project.",
    ownerID: 1,
    creationTime: 1609459200, // Example timestamp (Unix epoch time)
    color: "#FF5733", // Example color (Hex code)
    accessLevel: "WRITE"
  };
  
  projectEntry: DashboardEntry;

  constructor() {
    this.projectEntry = new DashboardEntry(this.sampleProject);
  }
}
