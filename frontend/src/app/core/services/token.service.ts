import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const TOKEN_KEY = 'accessToken';

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  /**
   * Lưu access token vào sessionStorage (chỉ thực hiện trên môi trường trình duyệt)
   */
  saveToken(token: string): void {
    if (this.isBrowser) {
      sessionStorage.setItem(TOKEN_KEY, token);
    }
  }

  /**
   * Lấy access token từ sessionStorage
   */
  getToken(): string | null {
    if (this.isBrowser) {
      return sessionStorage.getItem(TOKEN_KEY);
    }
    return null;
  }

  /**
   * Xóa access token khỏi sessionStorage
   */
  removeToken(): void {
    if (this.isBrowser) {
      sessionStorage.removeItem(TOKEN_KEY);
    }
  }

  /**
   * Giải mã payload của JWT string
   */
  decodeToken(token?: string): any | null {
    const targetToken = token || this.getToken();
    if (!targetToken) {
      return null;
    }

    try {
      const parts = targetToken.split('.');
      if (parts.length !== 3) {
        return null;
      }
      // Xử lý Base64URL sang Base64 chuẩn
      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join(''),
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }

  /**
   * Kiểm tra token đã hết hạn chưa (buffer time mặc định là 10 giây)
   */
  isTokenExpired(token?: string, bufferSeconds = 10): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) {
      return true;
    }

    const currentTimeSeconds = Math.floor(Date.now() / 1000);
    return decoded.exp <= currentTimeSeconds + bufferSeconds;
  }

  /**
   * Kiểm tra token hiện tại có tồn tại và còn hợp lệ (chưa hết hạn) hay không
   */
  isValidToken(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    return !this.isTokenExpired(token);
  }
}
