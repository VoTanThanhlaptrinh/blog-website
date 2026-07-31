import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  const user = authService.currentUser();
  if (!user) {
    toastService.error('Vui lòng đăng nhập để truy cập trang này.');
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }

  const isAdmin = user.roles && (user.roles.includes('ROLE_ADMIN') || user.roles.includes('ADMIN'));
  if (isAdmin) {
    return true;
  }

  toastService.error('Bạn không có quyền truy cập trang quản trị.');
  return router.createUrlTree(['/']);
};
