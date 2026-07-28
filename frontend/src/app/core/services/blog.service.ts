import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import {
  ApiResponse,
  BlogQueryParams,
  BlogResponse,
  PageResponse,
} from '../models/blog.model';

@Injectable({
  providedIn: 'root',
})
export class BlogService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/blogs';

  // State subjects
  private readonly blogsSubject = new BehaviorSubject<BlogResponse[]>([]);
  readonly blogs$ = this.blogsSubject.asObservable();

  private readonly pageMetaSubject = new BehaviorSubject<PageResponse<BlogResponse> | null>(null);
  readonly pageMeta$ = this.pageMetaSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  /**
   * Fetch paginated blogs list from API and update state subjects
   */
  getBlogs(params: BlogQueryParams = {}): Observable<PageResponse<BlogResponse>> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let httpParams = new HttpParams();
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.userId) httpParams = httpParams.set('userId', params.userId.toString());
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<ApiResponse<PageResponse<BlogResponse>>>(this.apiUrl, { params: httpParams }).pipe(
      map((res) => res.data),
      tap((pageData) => {
        this.blogsSubject.next(pageData.content || []);
        this.pageMetaSubject.next(pageData);
        this.loadingSubject.next(false);
      }),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể tải danh sách bài viết';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }

  /**
   * Fetch single blog detail by ID
   */
  getBlogById(id: number | string): Observable<BlogResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<ApiResponse<BlogResponse>>(`${this.apiUrl}/${id}`).pipe(
      map((res) => res.data),
      tap(() => this.loadingSubject.next(false)),
      catchError((err) => {
        const errorMessage = err?.error?.message || 'Không thể lấy thông tin bài viết';
        this.errorSubject.next(errorMessage);
        this.loadingSubject.next(false);
        return throwError(() => err);
      })
    );
  }
}
