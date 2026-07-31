import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { finalize } from 'rxjs';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-account-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-settings.component.html'
})
export class AccountSettingsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);

  email = signal<string>('');
  loading = signal(false);
  message = signal<string | null>(null);

  ngOnInit(): void {
    this.authService.getProfile().subscribe((res) => {
      if (res.data) {
        this.email.set(res.data.email || '');
      }
    });
  }

  async onDeactivate(): Promise<void> {
    const confirmed = await this.confirmService.confirm({
      title: 'Vô hiệu hóa tài khoản',
      message: 'Bạn có chắc chắn muốn vô hiệu hóa tài khoản? Hành động này sẽ đăng xuất bạn khỏi hệ thống.',
      confirmText: 'Vô hiệu hóa',
      actionType: 'danger'
    });

    if (!confirmed) return;

    this.loading.set(true);
    this.message.set(null);

    this.authService
      .deactivateAccount()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(() => {
        this.toastService.success('Tài khoản của bạn đã được vô hiệu hóa thành công.');
        this.authService.logout().subscribe();
      });
  }
}
