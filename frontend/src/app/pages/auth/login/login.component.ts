import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);

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

  submit() {
    this.errorMessage.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    // TODO: gọi AuthService.login() tới POST /api/v1/auth/login
    setTimeout(() => {
      this.submitting.set(false);
    }, 800);
  }
}
