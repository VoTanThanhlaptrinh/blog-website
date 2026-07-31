import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FileService } from '../../../core/services/file.service';
import { UserProfileResponse } from '../../../core/models/auth.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-update-profile',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './update-profile.component.html',
  styleUrl: './update-profile.component.scss',
})
export class UpdateProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);

  @ViewChild('avatarInput') avatarInput!: ElementRef<HTMLInputElement>;

  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly uploadingAvatar = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly avatarUrl = signal<string | null>(null);

  readonly form = this.fb.group({
    phone: [''],
    birthDate: [''],
    bio: [''],
  });

  ngOnInit(): void {
    this.authService
      .getProfile()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((res) => {
        if (res?.data) {
          this.populateForm(res.data);
        }
      });
  }

  private populateForm(profile: UserProfileResponse): void {
    this.form.patchValue({
      phone: profile.phone || '',
      birthDate: profile.birthDate || '',
      bio: profile.bio || '',
    });
    if (profile.avatarUrl) {
      this.avatarUrl.set(profile.avatarUrl);
    }
  }

  triggerAvatarPicker(): void {
    this.avatarInput.nativeElement.click();
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.uploadingAvatar.set(true);
    this.errorMessage.set(null);

    // Dùng FileService upload ảnh R2
    this.fileService
      .uploadFileToR2(file, 'profile/avatar')
      .pipe(finalize(() => this.uploadingAvatar.set(false)))
      .subscribe((publicUrl: string) => {
        this.avatarUrl.set(publicUrl);
      });

    input.value = '';
  }

  submit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.submitting.set(true);

    const { phone, birthDate, bio } = this.form.value;

    this.authService
      .updateProfile({
        phone: phone || undefined,
        birthDate: birthDate || undefined,
        bio: bio || undefined,
        avatarUrl: this.avatarUrl() || undefined,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe((res) => {
        this.successMessage.set(res?.message || 'Cập nhật thông tin cá nhân thành công!');
      });
  }
}

