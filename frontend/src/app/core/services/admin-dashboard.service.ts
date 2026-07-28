import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { ApiResponse, BlogResponse } from '../models/blog.model';
import { AdminDashboardSummaryResponse, DailyGrowthResponse } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminDashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/admin/dashboard';

  private readonly summarySubject = new BehaviorSubject<AdminDashboardSummaryResponse | null>(null);
  readonly summary$ = this.summarySubject.asObservable();

  private readonly growthStatsSubject = new BehaviorSubject<DailyGrowthResponse[]>([]);
  readonly growthStats$ = this.growthStatsSubject.asObservable();

  private readonly topBlogsSubject = new BehaviorSubject<BlogResponse[]>([]);
  readonly topBlogs$ = this.topBlogsSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  getDashboardSummary(): Observable<AdminDashboardSummaryResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<ApiResponse<AdminDashboardSummaryResponse>>(`${this.apiUrl}/summary`).pipe(
      map((res) => res.data),
      tap((summary) => {
        this.summarySubject.next(summary);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy tổng quan số liệu thống kê';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  getGrowthStats(days: number = 30): Observable<DailyGrowthResponse[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    const params = new HttpParams().set('days', days.toString());

    return this.http.get<ApiResponse<DailyGrowthResponse[]>>(`${this.apiUrl}/growth`, { params }).pipe(
      map((res) => res.data),
      tap((stats) => {
        this.growthStatsSubject.next(stats);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy dữ liệu tăng trưởng';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  getTopBlogs(limit: number = 10): Observable<BlogResponse[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    const params = new HttpParams().set('limit', limit.toString());

    return this.http.get<ApiResponse<BlogResponse[]>>(`${this.apiUrl}/top-blogs`, { params }).pipe(
      map((res) => res.data),
      tap((blogs) => {
        this.topBlogsSubject.next(blogs);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy danh sách bài viết hàng đầu';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }
}
