import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminSystemSettingService } from '../../../core/services/admin-system-setting.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-system-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-system-settings.component.html',
  styleUrl: './admin-system-settings.component.scss'
})
export class AdminSystemSettingsComponent implements OnInit {
  private readonly settingService = inject(AdminSystemSettingService);
  private readonly toastService = inject(ToastService);

  loading: boolean = false;
  error: string | null = null;

  settings: Record<string, string> = {
    siteName: 'B-BlogHub Editorial',
    siteDescription: 'A modern editorial engine for technical writers and developers.',
    maintenanceMode: 'false',
    maxUploadSizeMb: '10'
  };

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.loading = true;
    this.error = null;
    this.settingService.getSettings().subscribe({
      next: (res) => {
        if (res.data) {
          this.settings = { ...this.settings, ...res.data };
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Không thể tải cấu hình hệ thống';
        this.loading = false;
      }
    });
  }

  toggleMaintenance(): void {
    this.settings['maintenanceMode'] = this.settings['maintenanceMode'] === 'true' ? 'false' : 'true';
  }

  saveSettings(): void {
    this.loading = true;
    this.settingService.updateSettings(this.settings).subscribe({
      next: (res) => {
        if (res.data) {
          this.settings = { ...this.settings, ...res.data };
        }
        this.toastService.success('Đã lưu cấu hình hệ thống thành công.');
        this.loading = false;
      },
      error: (err) => {
        this.toastService.error(err?.error?.message || 'Lỗi khi lưu cấu hình');
        this.loading = false;
      }
    });
  }
}
