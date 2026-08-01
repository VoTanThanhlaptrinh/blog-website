import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RejectReasonModalComponent } from './reject-reason-modal.component';

describe('RejectReasonModalComponent', () => {
  let component: RejectReasonModalComponent;
  let fixture: ComponentFixture<RejectReasonModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RejectReasonModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RejectReasonModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
