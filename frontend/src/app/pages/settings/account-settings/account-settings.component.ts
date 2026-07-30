import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-account-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-settings.component.html'
})
export class AccountSettingsComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  email = signal<string>('');
  loading = signal(false);
  message = signal<string | null>(null);

  ngOnInit(): void {
    this.authService.getProfile().subscribe({
      next: (res) => {
        if (res.data) {
          this.email.set(res.data.email || '');
        }
      }
    });
  }

  onDeactivate(): void {
    if (!confirm('Bạn có chắc chắn muốn vô hiệu hóa tài khoản?')) return;

    this.loading.set(true);
    this.message.set(null);

    this.authService.deactivateAccount().subscribe({
      next: () => {
        this.loading.set(false);
        this.toastService.success('Tài khoản của bạn đã được vô hiệu hóa thành công.');
        this.authService.logout().subscribe();
      },
      error: (err) => {
        this.loading.set(false);
        this.message.set(err.error?.message || 'Không thể vô hiệu hóa tài khoản.');
      }
    });
  }
}
