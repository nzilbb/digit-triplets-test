import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InstanceFieldComponent } from './instance-field.component';

describe('InstanceFieldComponent', () => {
  let component: InstanceFieldComponent;
  let fixture: ComponentFixture<InstanceFieldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InstanceFieldComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InstanceFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
