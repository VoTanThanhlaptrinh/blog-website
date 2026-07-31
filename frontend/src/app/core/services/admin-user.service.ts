import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/auth.model';
import { PageResponse } from '../models/blog.model';

export interface AdminUserResponse {
  id: number;
  email: string;
  phone?: string;
  bio?: string;
  avatarUrl?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED' | 'PENDING';
  roles: string[];
  postsCount: number;
  createdDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/admin/users';

  getUsers(
    role?: string,
    status?: string,
    keyword?: string,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PageResponse<AdminUserResponse>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (role) params = params.set('role', role);
    if (status) params = params.set('status', status);
    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<AdminUserResponse>>>(this.apiUrl, { params });
  }

  updateUserStatus(userId: number, status: string, reason?: string): Observable<ApiResponse<AdminUserResponse>> {
    return this.http.put<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/${userId}/status`, { status, reason });
  }

  updateUserRole(userId: number, role: string): Observable<ApiResponse<AdminUserResponse>> {
    return this.http.put<ApiResponse<AdminUserResponse>>(`${this.apiUrl}/${userId}/role`, { role });
  }

  exportUsers(role?: string, status?: string, keyword?: string): void {
    let params = new HttpParams();
    if (role) params = params.set('role', role);
    if (status) params = params.set('status', status);
    if (keyword) params = params.set('keyword', keyword);

    this.http.get(`${this.apiUrl}/export`, { params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'users_export.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }
}
