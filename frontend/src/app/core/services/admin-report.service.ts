import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { ApiResponse, PageResponse } from '../models/blog.model';
import { AdminReportQueryParams, ReportResponse, ResolveReportRequest } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminReportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/admin/reports';

  private readonly reportsSubject = new BehaviorSubject<ReportResponse[]>([]);
  readonly reports$ = this.reportsSubject.asObservable();

  private readonly pageMetaSubject = new BehaviorSubject<PageResponse<ReportResponse> | null>(null);
  readonly pageMeta$ = this.pageMetaSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  getReports(params: AdminReportQueryParams = {}): Observable<PageResponse<ReportResponse>> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let httpParams = new HttpParams();
    if (params.targetType) httpParams = httpParams.set('targetType', params.targetType);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<ApiResponse<PageResponse<ReportResponse>>>(this.apiUrl, { params: httpParams }).pipe(
      map((res) => res.data),
      tap((pageData) => {
        this.reportsSubject.next(pageData.content || []);
        this.pageMetaSubject.next(pageData);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy danh sách báo cáo';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  resolveReport(id: number | string, request: ResolveReportRequest): Observable<ReportResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<ApiResponse<ReportResponse>>(`${this.apiUrl}/${id}/resolve`, request).pipe(
      map((res) => res.data),
      tap((updatedReport) => {
        const currentReports = this.reportsSubject.getValue();
        const updatedList = currentReports.map((r) => (r.id === updatedReport.id ? updatedReport : r));
        this.reportsSubject.next(updatedList);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể cập nhật trạng thái xử lý báo cáo';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  exportReports(targetType?: string, status?: string): void {
    let params = new HttpParams();
    if (targetType) params = params.set('targetType', targetType);
    if (status) params = params.set('status', status);

    this.http.get(`${this.apiUrl}/export`, { params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'reports_export.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }
}
