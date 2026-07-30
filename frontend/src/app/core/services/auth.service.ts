import { Injectable, effect, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, finalize } from 'rxjs';
import {
  AccountLoginRequest,
  ApiResponse,
  AuthResponse,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  RegisterRequest,
  ResetPasswordRequest,
  UpdateProfileRequest,
  UserProfileResponse,
  VerifyOtpRequest,
} from '../models/auth.model';
import { UploadPostResponse, UploadUrlRequest } from '../models/file.model';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly apiUrl = '/api/v1/auth';

  readonly currentUser = signal<UserProfileResponse | null | undefined>(undefined);

  /**
   * Đăng nhập mạng xã hội (Google / Facebook).
   * Chuyển hướng trình duyệt đến endpoint OAuth2 của backend.
   */
  loginWithSocial(provider: 'google' | 'facebook'): void {
    window.location.href = `${environment.apiUrl}/oauth2/authorization/${provider}`;
  }

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
          if (res && res.data?.user) {
            this.currentUser.set(res.data.user);
          }
        }),
      );
  }

  /**
   * Lấy danh sách URL đăng nhập qua mạng xã hội (Google, Facebook)
   */
  getSocialLoginUrls(): Observable<ApiResponse<{ [key: string]: string }>> {
    return this.http.get<ApiResponse<{ [key: string]: string }>>(`${this.apiUrl}/login/social`);
  }

  /**
   * Đăng ký tài khoản mới.
   */
  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request);
  }

  /**
  * Lấy thông tin người dùng hiện tại(profile)
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
   * Cập nhật thông tin người dùng (profile)
   */
  updateProfile(request: UpdateProfileRequest): Observable<ApiResponse<UserProfileResponse>> {
    return this.http.put<ApiResponse<UserProfileResponse>>(`${this.apiUrl}/profile`, request).pipe(
      tap((res) => {
        if (res && res.data) {
          this.currentUser.set(res.data);
        }
      })
    );
  }

  /**
   * Lấy presigned URL upload avatar cá nhân
   */
  getAvatarUploadUrl(request: UploadUrlRequest): Observable<ApiResponse<UploadPostResponse>> {
    return this.http.post<ApiResponse<UploadPostResponse>>(`${this.apiUrl}/profile/avatar/upload-url`, request);
  }

  /**
   * Yêu cầu gửi mã OTP quên mật khẩu về email
   */
  forgotPassword(request: ForgotPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/forgot-password`, request);
  }

  /**
   * Xác thực mã OTP quên mật khẩu
   */
  verifyOtp(request: VerifyOtpRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/verify-otp`, request);
  }

  /**
   * Đặt lại mật khẩu mới
   */
  resetPassword(request: ResetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/reset-password`, request);
  }

  /**
   * Đổi mật khẩu
   */
  changePassword(request: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/change-password`, request);
  }

  /**
   * Kích hoạt tài khoản qua Token
   */
  activeAccount(token: string): Observable<ApiResponse<void>> {
    return this.http.get<ApiResponse<void>>(`${this.apiUrl}/activeAccount`, {
      params: { token },
    });
  }

  /**
   * Xử lý khi lỗi xác thực (401 không refresh được, hoặc lỗi khác khi gọi refresh):
   * 1. Xóa token trong sessionStorage
   * 2. Xóa state người dùng
   * 3. Chuyển hướng về trang /login kèm returnUrl
   */
  handleAuthError(): void {
    this.currentUser.set(null);

    const currentUrl = this.router.url;
    if (!currentUrl.includes('/login')) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl } });
    } else {
      this.router.navigate(['/login']);
    }
  }
}

