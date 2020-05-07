import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SoundCheckComponent } from './sound-check.component';

describe('SoundCheckComponent', () => {
  let component: SoundCheckComponent;
  let fixture: ComponentFixture<SoundCheckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SoundCheckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SoundCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
