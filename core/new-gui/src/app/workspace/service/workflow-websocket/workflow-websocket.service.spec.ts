import { TestBed } from "@angular/core/testing";

import { WorkflowWebsocketService } from "./workflow-websocket.service";
import {WorkflowActionService} from "../workflow-graph/model/workflow-action.service";

describe("WorkflowWebsocketService", () => {
  let service: WorkflowWebsocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        WorkflowActionService,
      ],
    });
    service = TestBed.inject(WorkflowWebsocketService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
