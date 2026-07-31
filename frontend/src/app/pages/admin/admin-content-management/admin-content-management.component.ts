import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminBlogService } from '../../../core/services/admin-blog.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../core/services/confirm.service';
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
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);

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

  async onApprove(id: number): Promise<void> {
    const confirmed = await this.confirmService.confirm({
      title: 'Phê duyệt bài viết',
      message: 'Bạn có chắc chắn muốn phê duyệt bài viết này không?',
      confirmText: 'Phê duyệt',
      actionType: 'primary'
    });

    if (confirmed) {
      this.adminBlogService.approveBlog(id).subscribe(() => {
        this.toastService.success('Đã phê duyệt bài viết thành công.');
        this.loadBlogs();
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
      this.toastService.warning('Vui lòng nhập lý do từ chối.');
      return;
    }

    this.adminBlogService.rejectBlog(this.rejectingBlogId, { reason: this.rejectReason }).subscribe(() => {
      this.rejectingBlogId = null;
      this.rejectReason = '';
      this.toastService.success('Đã từ chối bài viết.');
      this.loadBlogs();
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadBlogs();
  }
}
