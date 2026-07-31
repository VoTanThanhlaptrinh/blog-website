import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { ApiResponse, BlogResponse, PageResponse } from '../models/blog.model';
import { AdminBlogQueryParams, RejectBlogRequest } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminBlogService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/admin/blogs';

  private readonly blogsSubject = new BehaviorSubject<BlogResponse[]>([]);
  readonly blogs$ = this.blogsSubject.asObservable();

  private readonly pageMetaSubject = new BehaviorSubject<PageResponse<BlogResponse> | null>(null);
  readonly pageMeta$ = this.pageMetaSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  getBlogsForModeration(params: AdminBlogQueryParams = {}): Observable<PageResponse<BlogResponse>> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let httpParams = new HttpParams();
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<ApiResponse<PageResponse<BlogResponse>>>(this.apiUrl, { params: httpParams }).pipe(
      map((res) => res.data),
      tap((pageData) => {
        this.blogsSubject.next(pageData.content || []);
        this.pageMetaSubject.next(pageData);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy danh sách bài viết duyệt';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  approveBlog(id: number | string): Observable<BlogResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<ApiResponse<BlogResponse>>(`${this.apiUrl}/${id}/approve`, {}).pipe(
      map((res) => res.data),
      tap((updatedBlog) => {
        const currentBlogs = this.blogsSubject.getValue();
        const updatedList = currentBlogs.map((b) => (b.id === updatedBlog.id ? updatedBlog : b));
        this.blogsSubject.next(updatedList);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể phê duyệt bài viết';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  rejectBlog(id: number | string, request: RejectBlogRequest): Observable<BlogResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<ApiResponse<BlogResponse>>(`${this.apiUrl}/${id}/reject`, request).pipe(
      map((res) => res.data),
      tap((updatedBlog) => {
        const currentBlogs = this.blogsSubject.getValue();
        const updatedList = currentBlogs.map((b) => (b.id === updatedBlog.id ? updatedBlog : b));
        this.blogsSubject.next(updatedList);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể từ chối xuất bản bài viết';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  exportBlogs(status?: string, keyword?: string): void {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (keyword) params = params.set('keyword', keyword);

    this.http.get(`${this.apiUrl}/export`, { params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'blogs_export.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }
}
