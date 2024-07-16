import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReportPrintComponent } from "./report-print.component";
import { ResultExportResponse } from "../../../types/workflow-websocket.interface";
import { HttpClientTestingModule } from "@angular/common/http/testing";

describe("ReportPrintComponent", () => {
  let component: ReportPrintComponent;
  let fixture: ComponentFixture<ReportPrintComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReportPrintComponent],
      imports: [HttpClientTestingModule]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportPrintComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should log results on changes", () => {
    const results: ResultExportResponse[] = [
      { status: "success", message: "path/to/result1" },
      { status: "success", message: "path/to/result2" }
    ];
    spyOn(console, "log");
    component.results = results;
    component.ngOnChanges({
      results: {
        currentValue: results,
        previousValue: [],
        firstChange: true,
        isFirstChange: () => true
      }
    });
    expect(console.log).toHaveBeenCalledWith("Result exported to: path/to/result1");
    expect(console.log).toHaveBeenCalledWith("Result exported to: path/to/result2");
  });
});
