import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { FileService } from '../../../core/services/file.service';
import { UpdateProfileRequest } from '../../../core/models/auth.model';
import { finalize } from 'rxjs';

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
    this.authService.getProfile().subscribe((res) => {
      if (res.data) {
        this.avatarUrl.set(res.data.avatarUrl || null);
        this.profileForm.patchValue({
          phone: res.data.phone || '',
          bio: res.data.bio || '',
          birthDate: res.data.birthDate || '',
          avatarUrl: res.data.avatarUrl || ''
        });
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

    this.fileService
      .uploadFileToR2(file, 'profile/avatar')
      .pipe(finalize(() => this.uploadingAvatar.set(false)))
      .subscribe((publicUrl: string) => {
        this.avatarUrl.set(publicUrl);
        this.profileForm.patchValue({ avatarUrl: publicUrl });
      });

    input.value = '';
  }

  onSubmit(): void {
    if (this.profileForm.invalid) return;

    this.loading.set(true);
    this.message.set(null);
    this.isError.set(false);

    const req: UpdateProfileRequest = this.profileForm.value;

    this.authService
      .updateProfile(req)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe(() => {
        this.message.set('Cập nhật hồ sơ cá nhân thành công!');
      });
  }
}
