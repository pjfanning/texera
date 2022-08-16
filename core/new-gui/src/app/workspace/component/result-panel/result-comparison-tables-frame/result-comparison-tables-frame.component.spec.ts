import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultComparisonTablesFrameComponent } from './result-comparison-tables-frame.component';

describe("ResultComparisonTablesFrameComponent", () => {
  let component: ResultComparisonTablesFrameComponent;
  let fixture: ComponentFixture<ResultComparisonTablesFrameComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultComparisonTablesFrameComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultComparisonTablesFrameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
