import {WorkflowWebsocketService} from "../workflow-websocket/workflow-websocket.service";
import {TestBed} from "@angular/core/testing";

describe("TimeTravelService", () => {
  let service: WorkflowWebsocketService;

  beforeEach(() => {
    service = TestBed.inject(WorkflowWebsocketService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
