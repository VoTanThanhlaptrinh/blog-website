import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/auth.model';

export interface NotificationSettingResponse {
  followers: boolean;
  comments: boolean;
  likes: boolean;
  mentions: boolean;
  newsletter: boolean;
  features: boolean;
}

export interface UpdateNotificationSettingRequest {
  followers: boolean;
  comments: boolean;
  likes: boolean;
  mentions: boolean;
  newsletter: boolean;
  features: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SettingService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/settings/notifications';

  getNotificationSettings(): Observable<ApiResponse<NotificationSettingResponse>> {
    return this.http.get<ApiResponse<NotificationSettingResponse>>(this.apiUrl);
  }

  updateNotificationSettings(request: UpdateNotificationSettingRequest): Observable<ApiResponse<NotificationSettingResponse>> {
    return this.http.put<ApiResponse<NotificationSettingResponse>>(this.apiUrl, request);
  }
}
