import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminArticleReviewComponent } from './admin-article-review.component';

describe('AdminArticleReviewComponent', () => {
  let component: AdminArticleReviewComponent;
  let fixture: ComponentFixture<AdminArticleReviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminArticleReviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminArticleReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
