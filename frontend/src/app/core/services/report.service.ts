import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { ApiResponse } from '../models/blog.model';
import { CreateReportRequest, ReportResponse } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/reports';

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  createReport(request: CreateReportRequest): Observable<ReportResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.post<ApiResponse<ReportResponse>>(this.apiUrl, request).pipe(
      map((res) => res.data),
      tap(() => this.loadingSubject.next(false)),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể gửi báo cáo';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }
}
