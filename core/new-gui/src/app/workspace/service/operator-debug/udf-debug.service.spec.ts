import { TestBed } from '@angular/core/testing';

import { UdfDebugService } from './udf-debug.service';

describe('UdfDebugService', () => {
  let service: UdfDebugService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UdfDebugService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
