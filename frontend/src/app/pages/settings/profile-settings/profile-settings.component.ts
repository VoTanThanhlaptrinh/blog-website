import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { FileService } from '../../../core/services/file.service';
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
  private readonly fileService = inject(FileService);

  profileForm!: FormGroup;
  loading = signal(false);
  uploadingAvatar = signal(false);
  avatarUrl = signal<string | null>(null);
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
          this.avatarUrl.set(res.data.avatarUrl || null);
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

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploadingAvatar.set(true);
    this.message.set(null);
    this.isError.set(false);

    this.fileService.uploadFileToR2(file, 'profile/avatar').subscribe({
      next: (publicUrl: string) => {
        this.uploadingAvatar.set(false);
        this.avatarUrl.set(publicUrl);
        this.profileForm.patchValue({ avatarUrl: publicUrl });
      },
      error: (err) => {
        this.uploadingAvatar.set(false);
        this.isError.set(true);
        this.message.set(err?.error?.message || 'Không thể upload ảnh avatar. Vui lòng thử lại.');
      }
    });

    input.value = '';
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
