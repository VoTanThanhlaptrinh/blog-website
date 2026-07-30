import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-active-account',
  imports: [RouterLink],
  templateUrl: './active-account.component.html',
})
export class ActiveAccountComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  readonly loading = signal(true);
  readonly success = signal(false);
  readonly message = signal<string>('Đang xác nhận kích hoạt tài khoản...');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.loading.set(false);
      this.success.set(false);
      this.message.set('Không tìm thấy mã token kích hoạt trong liên kết.');
      return;
    }

    this.authService.activeAccount(token).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.success.set(true);
        this.message.set(res?.message || 'Kích hoạt tài khoản thành công! Bạn hiện có thể đăng nhập.');
      },
      error: (err) => {
        this.loading.set(false);
        this.success.set(false);
        this.message.set(err?.error?.message || 'Kích hoạt tài khoản thất bại. Liên kết có thể đã hết hạn hoặc không hợp lệ.');
      },
    });
  }
}
