import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly sendingOtp = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    otp: ['', [Validators.required]],
  });

  sendOtp(): void {
    const emailControl = this.form.controls.email;
    if (emailControl.invalid) {
      emailControl.markAsTouched();
      this.errorMessage.set('Vui lòng nhập địa chỉ email hợp lệ.');
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.sendingOtp.set(true);

    this.authService.forgotPassword({ email: emailControl.value }).subscribe({
      next: (res) => {
        this.sendingOtp.set(false);
        this.successMessage.set(res?.message || 'Mã OTP đã được gửi về email của bạn.');
      },
      error: (err) => {
        this.sendingOtp.set(false);
        this.errorMessage.set(err?.error?.message || 'Không thể gửi mã OTP. Vui lòng kiểm tra lại email.');
      },
    });
  }

  verifyOtp(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Vui lòng nhập đầy đủ Email và Mã xác nhận.');
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);

    const { email, otp } = this.form.getRawValue();

    this.authService.verifyOtp({ email, otp }).subscribe({
      next: (res) => {
        this.submitting.set(false);
        const resetToken = res?.data;
        if (resetToken) {
          // Chuyển hướng sang trang Reset Password kèm token
          this.router.navigate(['/auth/reset-password'], { queryParams: { token: resetToken } });
        } else {
          this.errorMessage.set('Mã xác nhận không hợp lệ.');
        }
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Mã xác nhận không chính xác hoặc đã hết hạn.');
      },
    });
  }
}

