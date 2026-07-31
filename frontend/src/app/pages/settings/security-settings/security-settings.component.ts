import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ChangePasswordRequest } from '../../../core/models/auth.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-security-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './security-settings.component.html'
})
export class SecuritySettingsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  passwordForm!: FormGroup;
  loading = signal(false);
  message = signal<string | null>(null);
  isError = signal(false);

  ngOnInit(): void {
    this.passwordForm = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) return;

    const { oldPassword, newPassword, confirmPassword } = this.passwordForm.value;

    if (newPassword !== confirmPassword) {
      this.isError.set(true);
      this.message.set('Mật khẩu mới và xác nhận mật khẩu không trùng khớp.');
      return;
    }

    this.loading.set(true);
    this.message.set(null);
    this.isError.set(false);

    const req: ChangePasswordRequest = { oldPassword, newPassword };

    this.authService
      .changePassword(req)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(() => {
        this.message.set('Đổi mật khẩu thành công!');
        this.passwordForm.reset();
      });
  }
}
