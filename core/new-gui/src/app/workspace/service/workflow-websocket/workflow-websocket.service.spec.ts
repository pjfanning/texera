import { TestBed } from "@angular/core/testing";
import { HttpClient } from "@angular/common/http";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { WorkflowWebsocketService } from "./workflow-websocket.service";
import { WorkflowActionService } from "../workflow-graph/model/workflow-action.service";
import { WorkflowUtilService } from "../workflow-graph/util/workflow-util.service";
import { UndoRedoService } from "../undo-redo/undo-redo.service";
import { JointUIService } from "../joint-ui/joint-ui.service";
import { OperatorMetadataService } from "../operator-metadata/operator-metadata.service";
import { StubOperatorMetadataService } from "../operator-metadata/stub-operator-metadata.service";

describe("WorkflowWebsocketService", () => {
  let service: WorkflowWebsocketService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: OperatorMetadataService,
          useClass: StubOperatorMetadataService,
        },
        WorkflowActionService,
        WorkflowUtilService,
        UndoRedoService,
        JointUIService,
        WorkflowWebsocketService
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(WorkflowWebsocketService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
