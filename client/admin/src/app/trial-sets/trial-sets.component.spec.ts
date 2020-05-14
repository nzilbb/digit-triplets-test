import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TrialSetsComponent } from './trial-sets.component';

describe('TrialSetsComponent', () => {
  let component: TrialSetsComponent;
  let fixture: ComponentFixture<TrialSetsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TrialSetsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TrialSetsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
