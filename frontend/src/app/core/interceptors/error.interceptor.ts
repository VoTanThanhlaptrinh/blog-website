import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 403) {
        toastService.error('Bạn không có quyền truy cập. Đang chuyển hướng về trang chủ...', 'Truy cập bị từ chối');
        router.navigate(['/']);
      }
      return throwError(() => error);
    })
  );
};
