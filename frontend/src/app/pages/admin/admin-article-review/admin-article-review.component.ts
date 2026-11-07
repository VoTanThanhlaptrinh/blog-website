import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { RejectReasonModalComponent } from '../components/reject-reason-modal/reject-reason-modal.component';
import { AdminBlogService } from '../../../core/services/admin-blog.service';
import { ToastService } from '../../../core/services/toast.service';
import { BlogResponse } from '../../../core/models/blog.model';

@Component({
  selector: 'app-admin-article-review',
  imports: [CommonModule, RejectReasonModalComponent],
  templateUrl: './admin-article-review.component.html',
  styleUrl: './admin-article-review.component.scss'
})
export class AdminArticleReviewComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly adminBlogService = inject(AdminBlogService);
  private readonly toastService = inject(ToastService);

  article: BlogResponse | null = null;
  isLoading = true;
  error: string | null = null;
  
  isRejectModalOpen = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadArticle(id);
    } else {
      this.error = 'Không tìm thấy ID bài viết';
      this.isLoading = false;
    }
  }

  loadArticle(id: string): void {
    this.adminBlogService.getBlogById(id).subscribe({
      next: (data) => {
        this.article = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Lỗi khi tải bài viết';
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.location.back();
  }

  openRejectModal() {
    this.isRejectModalOpen = true;
  }

  closeRejectModal() {
    this.isRejectModalOpen = false;
  }

  onRejectConfirm(reason: string) {
    if (!this.article) return;
    this.adminBlogService.rejectBlog(this.article.id, { reason }).subscribe({
      next: () => {
        this.toastService.success('Đã từ chối bài viết thành công');
        this.isRejectModalOpen = false;
        this.goBack();
      },
      error: () => {
        this.isRejectModalOpen = false;
      }
    });
  }

  onApprove() {
    if (!this.article) return;
    this.adminBlogService.approveBlog(this.article.id).subscribe({
      next: () => {
        this.toastService.success('Đã duyệt bài viết thành công');
        this.goBack();
      }
    });
  }
}
