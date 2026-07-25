import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, finalize, catchError, of } from 'rxjs';
import { TokenService } from './token.service';
import {
  AccountLoginRequest,
  ApiResponse,
  AuthResponse,
  RegisterRequest,
  UserProfileResponse,
} from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);
  private readonly router = inject(Router);

  private readonly apiUrl = '/api/v1/auth';

  readonly currentUser = signal<UserProfileResponse | null>(null);

  /**
   * Đăng nhập tài khoản.
   * Yêu cầu gửi kèm withCredentials: true để nhận Cookie từ Server nếu có.
   */
  login(request: AccountLoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request, {
        withCredentials: true,
      })
      .pipe(
        tap((res) => {
          if (res && res.data?.accessToken) {
            this.tokenService.saveToken(res.data.accessToken);
            if (res.data.user) {
              this.currentUser.set(res.data.user);
            }
          }
        }),
      );
  }

  /**
   * Đăng ký tài khoản mới.
   */
  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request);
  }

  /**
   * Gọi API Refresh Token với Cookie refreshToken.
   * Nếu thành công, lưu lại accessToken mới vào TokenService.
   */
  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(
        `${this.apiUrl}/refresh`,
        {},
        { withCredentials: true },
      )
      .pipe(
        tap((res) => {
          if (res && res.data?.accessToken) {
            this.tokenService.saveToken(res.data.accessToken);
            if (res.data.user) {
              this.currentUser.set(res.data.user);
            }
          }
        }),
      );
  }

  /**
   * Lấy thông tin người dùng hiện tại (profile)
   */
  getProfile(): Observable<ApiResponse<UserProfileResponse>> {
    return this.http.get<ApiResponse<UserProfileResponse>>(`${this.apiUrl}/profile`).pipe(
      tap((res) => {
        if (res && res.data) {
          this.currentUser.set(res.data);
        }
      }),
    );
  }

  /**
   * Đăng xuất khỏi hệ thống
   */
  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .pipe(
        finalize(() => {
          this.handleAuthError();
        }),
      );
  }

  /**
   * Xử lý khi lỗi xác thực (401 không refresh được, hoặc lỗi khác khi gọi refresh):
   * 1. Xóa token trong sessionStorage
   * 2. Xóa state người dùng
   * 3. Chuyển hướng về trang /login kèm returnUrl
   */
  handleAuthError(): void {
    this.tokenService.removeToken();
    this.currentUser.set(null);

    const currentUrl = this.router.url;
    if (!currentUrl.includes('/login')) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl } });
    } else {
      this.router.navigate(['/login']);
    }
  }
}
