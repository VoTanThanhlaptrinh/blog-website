import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authGuard: CanActivateFn = (route, state) => {
  const platformId = inject(PLATFORM_ID);
  
  if (!isPlatformBrowser(platformId)) {
    return true; // Bỏ qua kiểm tra guard khi đang render trên Server (SSR)
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  const user = authService.currentUser();
  if (user) {
    return true;
  }
  toastService.error('Vui lòng đăng nhập để thực hiện chức năng này.');
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
