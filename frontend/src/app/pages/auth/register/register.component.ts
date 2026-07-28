import { Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';


function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  if (!confirm) {
    return null;
  }
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);


  readonly benefits = [
    'Miễn phí trọn đời, không giới hạn số bài viết',
    'Trình soạn thảo Markdown gọn nhẹ, xem trước tức thì',
    'Kết nối với hàng nghìn lập trình viên khác',
  ];
  readonly brandStats = [
    { value: '12K+', label: 'Bài viết' },
    { value: '4.8K+', label: 'Tác giả' },
    { value: '95K+', label: 'Yêu thích' },
  ];

  readonly showPassword = signal(false);
  readonly showConfirm = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      agree: [false, [Validators.requiredTrue]],
    },
    { validators: passwordsMatch },
  );

  readonly passwordValue = signal('');
  readonly strength = computed(() => {
    const value = this.passwordValue();
    let score = 0;
    if (value.length >= 6) score++;
    if (value.length >= 10) score++;
    if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++;
    if (/\d/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;
    return Math.min(score, 4);
  });
  readonly strengthLabel = computed(
    () => ['Rất yếu', 'Yếu', 'Trung bình', 'Khá', 'Mạnh'][this.strength()],
  );

  constructor() {
    this.form.controls.password.valueChanges.subscribe((v: string) =>
      this.passwordValue.set(v ?? ''),
    );
  }


  togglePassword() {
    this.showPassword.update((v) => !v);
  }

  toggleConfirm() {
    this.showConfirm.update((v) => !v);
  }

  hasError(control: 'email' | 'password' | 'confirmPassword'): boolean {
    const c = this.form.controls[control];
    return c.invalid && (c.dirty || c.touched);
  }

  get confirmMismatch(): boolean {
    const c = this.form.controls.confirmPassword;
    return (
      this.form.hasError('passwordMismatch') && (c.dirty || c.touched) && !!c.value
    );
  }

  submit() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const { email, password, confirmPassword } = this.form.getRawValue();

    this.authService.register({ email, password, confirmPassword }).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.successMessage.set(res?.message || 'Đăng ký tài khoản thành công! Vui lòng kiểm tra email để kích hoạt.');
        this.form.reset();
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Đăng ký không thành công. Vui lòng thử lại.');
      },
    });
  }
}

