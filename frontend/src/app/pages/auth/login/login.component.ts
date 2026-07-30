import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly year = new Date().getFullYear();
  readonly highlights = [
    'Đăng bài và chia sẻ kiến thức của bạn',
    'Thảo luận cùng cộng đồng lập trình viên',
    'Lưu và yêu thích những nội dung hay',
  ];
  readonly brandStats = [
    { value: '12K+', label: 'Bài viết' },
    { value: '4.8K+', label: 'Tác giả' },
    { value: '95K+', label: 'Yêu thích' },
  ];
  readonly testimonial = {
    quote:
      'BlogHub là nơi mình học được nhiều nhất từ cộng đồng. Giao diện gọn gàng, viết bài cực nhanh.',
    author: 'Nguyễn Văn A',
    role: 'Senior Developer',
    initial: 'A',
  };

  readonly showPassword = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [false],
  });

  togglePassword() {
    this.showPassword.update((v) => !v);
  }

  hasError(control: 'email' | 'password'): boolean {
    const c = this.form.controls[control];
    return c.invalid && (c.dirty || c.touched);
  }

  loginWithGoogle() {
    this.authService.loginWithSocial('google');
  }

  loginWithFacebook() {
    this.authService.loginWithSocial('facebook');
  }

  submit() {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);

    const formValue = this.form.getRawValue();
    this.authService.login(formValue).subscribe({
      next: (res) => {
        this.submitting.set(false);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg = err?.error?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin!';
        this.errorMessage.set(msg);
      },
    });
  }
}
