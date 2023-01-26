import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ReplayDisplayComponent } from "./replay-display.component";

describe("ReplayDisplayComponent", () => {
  let component: ReplayDisplayComponent;
  let fixture: ComponentFixture<ReplayDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReplayDisplayComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReplayDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
