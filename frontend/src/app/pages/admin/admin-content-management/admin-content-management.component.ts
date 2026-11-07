import { Component, OnInit, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminBlogService } from '../../../core/services/admin-blog.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../core/services/confirm.service';
import { BlogStatus } from '../../../core/models/blog.model';

@Component({
  selector: 'app-admin-content-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-content-management.component.html',
  styleUrl: './admin-content-management.component.scss'
})
export class AdminContentManagementComponent implements OnInit {
  protected readonly adminBlogService = inject(AdminBlogService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly blogs$ = this.adminBlogService.blogs$;
  readonly pageMeta$ = this.adminBlogService.pageMeta$;
  readonly loading$ = this.adminBlogService.loading$;
  readonly error$ = this.adminBlogService.error$;

  selectedStatus: string = BlogStatus.PENDING;
  searchKeyword: string = '';
  currentPage: number = 0;
  pageSize: number = 10;

  // Dropdown state
  activeDropdownBlogId: number | null = null;

  // Reject modal / inline state
  rejectingBlogId: number | null = null;
  rejectReason: string = '';

  readonly BlogStatus = BlogStatus;

  @HostListener('document:click')
  closeDropdown(): void {
    this.activeDropdownBlogId = null;
  }

  toggleDropdown(blogId: number, event: Event): void {
    event.stopPropagation();
    this.activeDropdownBlogId = this.activeDropdownBlogId === blogId ? null : blogId;
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.selectedStatus = params['status'] || BlogStatus.PENDING;
      this.searchKeyword = params['keyword'] || '';
      this.currentPage = params['page'] ? +params['page'] : 0;
      this.loadBlogs();
    });
  }

  loadBlogs(): void {
    this.adminBlogService.getBlogsForModeration({
      status: this.selectedStatus || undefined,
      keyword: this.searchKeyword || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe();
  }

  private updateQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        status: this.selectedStatus,
        keyword: this.searchKeyword || null,
        page: this.currentPage > 0 ? this.currentPage : null
      },
      queryParamsHandling: 'merge'
    });
  }

  onFilterStatus(status: string): void {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.updateQueryParams();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.updateQueryParams();
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

  showReason(reason?: string): void {
    this.confirmService.confirm({
      title: 'Lý do từ chối bài viết',
      message: reason || 'Không có lý do chi tiết được cung cấp.',
      confirmText: 'Đóng',
      actionType: 'primary'
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.updateQueryParams();
  }

  onExport(): void {
    this.adminBlogService.exportBlogs(this.selectedStatus || undefined, this.searchKeyword || undefined);
  }
}
