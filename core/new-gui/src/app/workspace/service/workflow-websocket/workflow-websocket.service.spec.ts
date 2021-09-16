import { TestBed } from "@angular/core/testing";

import { WorkflowWebsocketService } from "./workflow-websocket.service";
import { WorkflowActionService } from "../workflow-graph/model/workflow-action.service";
import { WorkflowUtilService } from "../workflow-graph/util/workflow-util.service";
import { UndoRedoService } from "../undo-redo/undo-redo.service";
import { JointUIService } from "../joint-ui/joint-ui.service";
import { OperatorMetadataService } from "../operator-metadata/operator-metadata.service";
import { StubOperatorMetadataService } from "../operator-metadata/stub-operator-metadata.service";
import { HttpClient } from "@angular/common/http";
import { Observable, of } from "rxjs";

class StubHttpClient {
  constructor() {}

  public post(): Observable<string> {
    return of("a");
  }
}

describe("WorkflowWebsocketService", () => {
  let service: WorkflowWebsocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        WorkflowActionService,
        WorkflowUtilService,
        UndoRedoService,
        JointUIService,
        {
          provide: OperatorMetadataService,
          useClass: StubOperatorMetadataService,
        },
        { provide: HttpClient, useClass: StubHttpClient },
      ],
    });
    service = TestBed.inject(WorkflowWebsocketService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
