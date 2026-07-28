import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, of, tap } from 'rxjs';
import { ApiResponse } from '../models/blog.model';
import { NotificationResponse, NotificationType } from '../models/notification.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/notifications';

  private readonly notificationsSubject = new BehaviorSubject<NotificationResponse[]>([]);
  readonly notifications$ = this.notificationsSubject.asObservable();

  private readonly unreadCountSubject = new BehaviorSubject<number>(
    this.notificationsSubject.value.filter((n) => !n.isRead).length
  );
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  /**
   * Fetch user notifications from API
   */
  getNotifications(): Observable<NotificationResponse[]> {
    this.loadingSubject.next(true);

    return this.http.get<ApiResponse<{ content: NotificationResponse[] }>>(this.apiUrl).pipe(
      map((res) => res.data?.content || []),
      tap((items) => {
        if (items && items.length > 0) {
          this.notificationsSubject.next(items);
          this.updateUnreadCount(items);
        }
        this.loadingSubject.next(false);
      }),
      catchError(() => {
        this.loadingSubject.next(false);
        return of([]);
      })
    );
  }

  /**
   * Fetch unread notification count
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/unread-count`).pipe(
      map((res) => res.data),
      tap((count) => this.unreadCountSubject.next(count)),
      catchError(() => of(0))
    );
  }

  /**
   * Mark a single notification as read
   */
  markAsRead(id: number): Observable<void> {
    // Optimistic UI update
    const current = this.notificationsSubject.value.map((n) =>
      n.id === id ? { ...n, isRead: true } : n
    );
    this.notificationsSubject.next(current);
    this.updateUnreadCount(current);

    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${id}/read`, {}).pipe(
      map(() => void 0),
      catchError(() => of(void 0))
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<void> {
    const current = this.notificationsSubject.value.map((n) => ({ ...n, isRead: true }));
    this.notificationsSubject.next(current);
    this.updateUnreadCount(current);

    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/read-all`, {}).pipe(
      map(() => void 0),
      catchError(() => of(void 0))
    );
  }

  private updateUnreadCount(items: NotificationResponse[]): void {
    const count = items.filter((n) => !n.isRead).length;
    this.unreadCountSubject.next(count);
  }

}
