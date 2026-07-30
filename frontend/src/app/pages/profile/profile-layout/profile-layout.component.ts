import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserProfileResponse } from '../../../core/models/auth.model';

@Component({
  selector: 'app-profile-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './profile-layout.component.html',
  styleUrl: './profile-layout.component.scss'
})
export class ProfileLayoutComponent implements OnInit {
  private readonly authService = inject(AuthService);
  readonly user = signal<UserProfileResponse | null>(null);

  ngOnInit(): void {
    const cachedUser = this.authService.currentUser();
    if (cachedUser) {
      this.user.set(cachedUser);
    }
    this.authService.getProfile().subscribe({
      next: (res) => {
        if (res?.data) {
          this.user.set(res.data);
        }
      },
      error: () => {}
    });
  }
}
