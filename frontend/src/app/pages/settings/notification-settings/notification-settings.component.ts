import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { SettingService, NotificationSettingResponse } from '../../../core/services/setting.service';

@Component({
  selector: 'app-notification-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './notification-settings.component.html',
  styleUrl: './notification-settings.component.scss'
})
export class NotificationSettingsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly settingService = inject(SettingService);

  settingsForm!: FormGroup;
  loading = signal(false);
  saving = signal(false);
  message = signal<string | null>(null);
  isError = signal(false);

  ngOnInit(): void {
    this.settingsForm = this.fb.group({
      followers: [true],
      comments: [true],
      likes: [false],
      mentions: [true],
      newsletter: [true],
      features: [true]
    });

    this.loadSettings();
  }

  loadSettings(): void {
    this.loading.set(true);
    this.settingService.getNotificationSettings().subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.data) {
          this.settingsForm.patchValue(res.data);
        }
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    this.saving.set(true);
    this.message.set(null);
    this.isError.set(false);

    this.settingService.updateNotificationSettings(this.settingsForm.value).subscribe({
      next: () => {
        this.saving.set(false);
        this.message.set('Cài đặt thông báo đã được lưu thành công!');
      },
      error: (err) => {
        this.saving.set(false);
        this.isError.set(true);
        this.message.set(err.error?.message || 'Không thể lưu cài đặt thông báo.');
      }
    });
  }
}
