import { Injectable } from '@angular/core';
import {DashboardEnvironment} from "../../type/environment";

@Injectable({
  providedIn: 'root'  // This ensures the service is a singleton for the entire app.
})
export class EnvironmentService {
  private environments: DashboardEnvironment[] = [];

  constructor() {}

  // Create: Add a new environment
  addEnvironment(environment: DashboardEnvironment): void {
    environment.environment.eid = this.environments.length; // Set the eid as the index
    this.environments.push(environment);
  }

  getAllEnvironments(): DashboardEnvironment[] {
    return this.environments;
  }

  // Read: Get an environment by its index (eid)
  getEnvironmentByIndex(index: number): DashboardEnvironment | null {
    if (index >= 0 && index < this.environments.length) {
      return this.environments[index];
    }
    return null; // Return null if index out of bounds
  }

  // Update: Modify an existing environment by its index (eid)
  updateEnvironment(index: number, updatedEnvironment: DashboardEnvironment): void {
    if (index >= 0 && index < this.environments.length) {
      this.environments[index] = updatedEnvironment;
    } else {
      throw new Error('Environment index out of bounds');
    }
  }

  // Delete: Remove an environment by its index (eid)
  deleteEnvironment(index: number): void {
    if (index >= 0 && index < this.environments.length) {
      this.environments.splice(index, 1);
      // Re-assign EIDs for subsequent items
      for (let i = index; i < this.environments.length; i++) {
        this.environments[i].environment.eid = i;
      }
    } else {
      throw new Error('Environment index out of bounds');
    }
  }
}
