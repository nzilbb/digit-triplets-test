import { TestBed } from '@angular/core/testing';

import { TrialSetService } from './trial-set.service';

describe('TrialSetService', () => {
  let service: TrialSetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrialSetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
