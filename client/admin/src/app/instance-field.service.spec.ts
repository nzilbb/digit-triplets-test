import { TestBed } from '@angular/core/testing';

import { InstanceFieldService } from './instance-field.service';

describe('InstanceFieldService', () => {
  let service: InstanceFieldService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InstanceFieldService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
