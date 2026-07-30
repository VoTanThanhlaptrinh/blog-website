import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  if (!confirm) {
    return null;
  }
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly token = signal<string>('');
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch }
  );

  ngOnInit(): void {
    const tokenFromUrl = this.route.snapshot.queryParamMap.get('token');
    if (tokenFromUrl) {
      this.token.set(tokenFromUrl);
    } else {
      this.errorMessage.set('Thiếu mã token xác thực. Vui lòng thực hiện lại quy trình Quên mật khẩu.');
    }
  }

  get passwordMismatch(): boolean {
    const c = this.form.controls.confirmPassword;
    return this.form.hasError('passwordMismatch') && (c.dirty || c.touched) && !!c.value;
  }

  submit(): void {
    if (!this.token()) {
      this.errorMessage.set('Thiếu mã token xác nhận. Không thể đặt lại mật khẩu.');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);

    const { newPassword, confirmPassword } = this.form.getRawValue();

    this.authService
      .resetPassword({
        token: this.token(),
        newPassword,
        confirmPassword,
      })
      .subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.successMessage.set(res?.message || 'Đặt lại mật khẩu thành công! Đang chuyển hướng về trang Đăng nhập...');
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Không thể đặt lại mật khẩu. Mã token có thể đã hết hạn.');
        },
      });
  }
}
