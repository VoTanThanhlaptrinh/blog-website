import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminReportService } from '../../../core/services/admin-report.service';
import { ToastService } from '../../../core/services/toast.service';
import { ReportStatus, ReportTargetType } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.scss'
})
export class AdminReportsComponent implements OnInit {
  protected readonly adminReportService = inject(AdminReportService);
  private readonly toastService = inject(ToastService);

  readonly reports$ = this.adminReportService.reports$;
  readonly pageMeta$ = this.adminReportService.pageMeta$;
  readonly loading$ = this.adminReportService.loading$;
  readonly error$ = this.adminReportService.error$;

  selectedTargetType?: ReportTargetType;
  selectedStatus?: ReportStatus;
  currentPage: number = 0;
  pageSize: number = 10;

  // Resolve dialog state
  resolvingReportId: number | null = null;
  resolveStatus: ReportStatus = ReportStatus.RESOLVED_ACCEPTED;
  adminNotes: string = '';

  readonly ReportStatus = ReportStatus;
  readonly ReportTargetType = ReportTargetType;

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.adminReportService.getReports({
      targetType: this.selectedTargetType,
      status: this.selectedStatus,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe();
  }

  onFilterTargetType(targetType?: ReportTargetType): void {
    this.selectedTargetType = targetType;
    this.currentPage = 0;
    this.loadReports();
  }

  onFilterStatus(status?: ReportStatus): void {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadReports();
  }

  openResolveDialog(id: number): void {
    this.resolvingReportId = id;
    this.resolveStatus = ReportStatus.RESOLVED_ACCEPTED;
    this.adminNotes = '';
  }

  cancelResolve(): void {
    this.resolvingReportId = null;
    this.adminNotes = '';
  }

  confirmResolve(): void {
    if (!this.resolvingReportId) return;

    this.adminReportService.resolveReport(this.resolvingReportId, {
      status: this.resolveStatus,
      adminNotes: this.adminNotes
    }).subscribe({
      next: () => {
        this.resolvingReportId = null;
        this.adminNotes = '';
        this.toastService.success('Đã cập nhật báo cáo thành công.');
        this.loadReports();
      },
      error: (err) => this.toastService.error(err?.error?.message || 'Có lỗi xảy ra khi cập nhật báo cáo.')
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadReports();
  }
}
