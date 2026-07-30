import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { UpdateProfileRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-profile-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile-settings.component.html'
})
export class ProfileSettingsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  profileForm!: FormGroup;
  loading = signal(false);
  message = signal<string | null>(null);
  isError = signal(false);

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      phone: [''],
      bio: [''],
      birthDate: [''],
      avatarUrl: ['']
    });

    this.loadProfile();
  }

  loadProfile(): void {
    this.authService.getProfile().subscribe({
      next: (res) => {
        if (res.data) {
          this.profileForm.patchValue({
            phone: res.data.phone || '',
            bio: res.data.bio || '',
            birthDate: res.data.birthDate || '',
            avatarUrl: res.data.avatarUrl || ''
          });
        }
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) return;

    this.loading.set(true);
    this.message.set(null);
    this.isError.set(false);

    const req: UpdateProfileRequest = this.profileForm.value;

    this.authService.updateProfile(req).subscribe({
      next: () => {
        this.loading.set(false);
        this.message.set('Cập nhật hồ sơ cá nhân thành công!');
      },
      error: (err) => {
        this.loading.set(false);
        this.isError.set(true);
        this.message.set(err.error?.message || 'Không thể cập nhật hồ sơ cá nhân.');
      }
    });
  }
}
