import { TestBed } from '@angular/core/testing';

import { DttService } from './dtt.service';

describe('DttService', () => {
  let service: DttService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DttService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
