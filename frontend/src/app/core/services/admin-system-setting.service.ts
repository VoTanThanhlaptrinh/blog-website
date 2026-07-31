import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AdminSystemSettingService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/admin/settings';

  getSettings(): Observable<ApiResponse<Record<string, string>>> {
    return this.http.get<ApiResponse<Record<string, string>>>(this.apiUrl);
  }

  updateSettings(settings: Record<string, string>): Observable<ApiResponse<Record<string, string>>> {
    return this.http.put<ApiResponse<Record<string, string>>>(this.apiUrl, settings);
  }
}
