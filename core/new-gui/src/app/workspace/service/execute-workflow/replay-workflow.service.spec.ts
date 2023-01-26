import { TestBed } from "@angular/core/testing";

import { ReplayWorkflowService } from "./replay-workflow.service";

describe("ReplayWorkflowService", () => {
  let service: ReplayWorkflowService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReplayWorkflowService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
