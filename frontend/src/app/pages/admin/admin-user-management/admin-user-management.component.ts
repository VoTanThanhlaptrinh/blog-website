import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUserService, AdminUserResponse } from '../../../core/services/admin-user.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../core/services/confirm.service';
import { PageResponse } from '../../../core/models/blog.model';

@Component({
  selector: 'app-admin-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-user-management.component.html',
  styleUrl: './admin-user-management.component.scss'
})
export class AdminUserManagementComponent implements OnInit {
  private readonly adminUserService = inject(AdminUserService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);

  users: AdminUserResponse[] = [];
  pageMeta: PageResponse<AdminUserResponse> | null = null;
  loading: boolean = false;
  error: string | null = null;

  selectedRole?: string;
  selectedStatus?: string;
  searchKeyword: string = '';
  currentPage: number = 0;
  pageSize: number = 10;

  // Edit role modal state
  editingUser: AdminUserResponse | null = null;
  newRole: string = 'USER';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    this.adminUserService.getUsers(
      this.selectedRole,
      this.selectedStatus,
      this.searchKeyword || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (res) => {
        this.users = res.data.content || [];
        this.pageMeta = res.data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Không thể tải danh sách người dùng';
        this.loading = false;
      }
    });
  }

  onFilterRole(role?: string): void {
    this.selectedRole = role;
    this.currentPage = 0;
    this.loadUsers();
  }

  onFilterStatus(status?: string): void {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadUsers();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  async onApprove(user: AdminUserResponse): Promise<void> {
    const confirmed = await this.confirmService.confirm({
      title: 'Kích hoạt tài khoản',
      message: `Bạn có chắc muốn duyệt/kích hoạt tài khoản ${user.email}?`,
      confirmText: 'Kích hoạt',
      actionType: 'primary'
    });

    if (confirmed) {
      this.adminUserService.updateUserStatus(user.id, 'ACTIVE').subscribe({
        next: () => {
          this.toastService.success('Đã kích hoạt tài khoản.');
          this.loadUsers();
        },
        error: (err) => this.toastService.error(err?.error?.message || 'Lỗi xử lý')
      });
    }
  }

  async onSuspend(user: AdminUserResponse): Promise<void> {
    const confirmed = await this.confirmService.confirm({
      title: 'Khóa tài khoản',
      message: `Bạn có chắc muốn khóa tài khoản ${user.email}?`,
      confirmText: 'Khóa tài khoản',
      actionType: 'danger'
    });

    if (confirmed) {
      this.adminUserService.updateUserStatus(user.id, 'BANNED').subscribe({
        next: () => {
          this.toastService.success('Đã khóa tài khoản.');
          this.loadUsers();
        },
        error: (err) => this.toastService.error(err?.error?.message || 'Lỗi xử lý')
      });
    }
  }

  openEditRole(user: AdminUserResponse): void {
    this.editingUser = user;
    this.newRole = user.roles[0] || 'USER';
  }

  cancelEditRole(): void {
    this.editingUser = null;
  }

  saveRole(): void {
    if (!this.editingUser) return;
    this.adminUserService.updateUserRole(this.editingUser.id, this.newRole).subscribe({
      next: () => {
        this.toastService.success('Đã cập nhật vai trò người dùng.');
        this.editingUser = null;
        this.loadUsers();
      },
      error: (err) => this.toastService.error(err?.error?.message || 'Lỗi cập nhật vai trò')
    });
  }

  onExport(): void {
    this.adminUserService.exportUsers(this.selectedRole, this.selectedStatus, this.searchKeyword || undefined);
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }
}
