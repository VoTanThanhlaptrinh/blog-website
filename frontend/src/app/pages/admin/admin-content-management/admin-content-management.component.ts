import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminBlogService } from '../../../core/services/admin-blog.service';
import { BlogStatus } from '../../../core/models/blog.model';

@Component({
  selector: 'app-admin-content-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-content-management.component.html',
  styleUrl: './admin-content-management.component.scss'
})
export class AdminContentManagementComponent implements OnInit {
  protected readonly adminBlogService = inject(AdminBlogService);

  readonly blogs$ = this.adminBlogService.blogs$;
  readonly pageMeta$ = this.adminBlogService.pageMeta$;
  readonly loading$ = this.adminBlogService.loading$;
  readonly error$ = this.adminBlogService.error$;

  selectedStatus: string = '';
  searchKeyword: string = '';
  currentPage: number = 0;
  pageSize: number = 10;

  // Reject modal / inline state
  rejectingBlogId: number | null = null;
  rejectReason: string = '';

  readonly BlogStatus = BlogStatus;

  ngOnInit(): void {
    this.loadBlogs();
  }

  loadBlogs(): void {
    this.adminBlogService.getBlogsForModeration({
      status: this.selectedStatus || undefined,
      keyword: this.searchKeyword || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe();
  }

  onFilterStatus(status: string): void {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadBlogs();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadBlogs();
  }

  onApprove(id: number): void {
    if (confirm('Bạn có chắc chắn muốn phê duyệt bài viết này không?')) {
      this.adminBlogService.approveBlog(id).subscribe({
        next: () => this.loadBlogs(),
        error: (err) => alert(err?.error?.message || 'Có lỗi xảy ra khi phê duyệt bài viết.')
      });
    }
  }

  openRejectDialog(id: number): void {
    this.rejectingBlogId = id;
    this.rejectReason = '';
  }

  cancelReject(): void {
    this.rejectingBlogId = null;
    this.rejectReason = '';
  }

  confirmReject(): void {
    if (!this.rejectingBlogId) return;
    if (!this.rejectReason.trim()) {
      alert('Vui lòng nhập lý do từ chối.');
      return;
    }

    this.adminBlogService.rejectBlog(this.rejectingBlogId, { reason: this.rejectReason }).subscribe({
      next: () => {
        this.rejectingBlogId = null;
        this.rejectReason = '';
        this.loadBlogs();
      },
      error: (err) => alert(err?.error?.message || 'Có lỗi xảy ra khi từ chối bài viết.')
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadBlogs();
  }
}
