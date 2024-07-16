import { TestBed } from "@angular/core/testing";
import { ReportPrintService } from "./report-print.service";
import { HttpClientTestingModule } from "@angular/common/http/testing";

describe("ReportPrintService", () => {
  let service: ReportPrintService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule], // 确保引入了HttpClientTestingModule
      providers: [ReportPrintService] // 确保提供了ReportPrintService
    });
    service = TestBed.inject(ReportPrintService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
