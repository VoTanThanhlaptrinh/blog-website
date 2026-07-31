import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { environment } from '../../../environments/environment';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);
  const listApiCatchError = ['/api/v1/auth/profile'];
  const currentApiUrl = req.url;
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 403) {
        toastService.error('Bạn không có quyền truy cập. Đang chuyển hướng về trang chủ...', 'Truy cập bị từ chối');
        router.navigate(['/']);
      }
      if (error.status === 401 && !listApiCatchError.map(item => environment.apiUrl + item).includes(currentApiUrl)) {
        toastService.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'Phiên đăng nhập hết hạn');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
