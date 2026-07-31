import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  template: `
    <div class="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-stone-50 p-4">
      <div class="flex flex-col items-center gap-4 rounded-xl border border-stone-200 bg-white p-8 shadow-sm">
        <svg class="h-8 w-8 animate-spin text-clay-600" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <p class="text-sm font-medium text-stone-700">Đang xử lý đăng nhập Mạng xã hội...</p>
      </div>
    </div>
  `
})
export class Oauth2RedirectComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  ngOnInit(): void {
    this.authService.getProfile().subscribe(() => {
      this.router.navigate(['/']);
    });
  }
}
