import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const AUTH_ENDPOINTS = [
  '/login',
  '/register',
  '/refresh',
  '/forgot-password',
  '/verify-otp',
  '/reset-password',
  '/activeAccount',
  '/login/social',
];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const authService = inject(AuthService);

  const isAuthEndpoint = AUTH_ENDPOINTS.some((url) => req.url.includes(url));
  const isExternalUrl = req.url.startsWith('http://') || req.url.startsWith('https://');

  let authReq = req;
  const token = tokenService.getToken();
  if (!isAuthEndpoint && !isExternalUrl && token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthEndpoint && !isExternalUrl) {
        return handle401Error(authReq, next, authService, tokenService);
      }

      return throwError(() => error);
    }),
  );
};

function handle401Error(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  tokenService: TokenService,
) {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap((res) => {
        isRefreshing = false;
        const newToken = res?.data?.accessToken || tokenService.getToken();
        if (newToken) {
          refreshTokenSubject.next(newToken);
          return next(
            req.clone({
              setHeaders: {
                Authorization: `Bearer ${newToken}`,
              },
            }),
          );
        }
        authService.handleAuthError();
        return throwError(() => new Error('Refresh token không trả về accessToken mới'));
      }),
      catchError((refreshErr) => {
        isRefreshing = false;
        authService.handleAuthError();
        return throwError(() => refreshErr);
      }),
    );
  } else {
    return refreshTokenSubject.pipe(
      filter((token) => token !== null),
      take(1),
      switchMap((token) => {
        return next(
          req.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`,
            },
          }),
        );
      }),
    );
  }
}
