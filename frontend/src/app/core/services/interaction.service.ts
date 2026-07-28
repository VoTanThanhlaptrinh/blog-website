import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiResponse, PageResponse, BlogResponse } from '../models/blog.model';
import {
  BookmarkResponse,
  CommentLikeResponse,
  CommentResponse,
  CreateCommentRequest,
  CreateShareRequest,
  LikeResponse,
  RecordViewRequest,
  ShareResponse,
  ToggleBookmarkRequest,
  ToggleCommentLikeRequest,
  ToggleLikeRequest,
  UpdateCommentRequest,
  ViewResponse,
} from '../models/interaction.model';

@Injectable({
  providedIn: 'root',
})
export class InteractionService {
  private readonly http = inject(HttpClient);

  private readonly viewsUrl = '/api/v1/views';
  private readonly likesUrl = '/api/v1/likes';
  private readonly commentsUrl = '/api/v1/comments';
  private readonly bookmarksUrl = '/api/v1/bookmarks';
  private readonly sharesUrl = '/api/v1/shares';

  /**
   * Record a view for a blog post
   */
  recordView(blogId: number): Observable<ViewResponse> {
    const body: RecordViewRequest = { blogId };
    return this.http
      .post<ApiResponse<ViewResponse>>(`${this.viewsUrl}/record`, body)
      .pipe(map((res) => res.data));
  }

  /**
   * Toggle like on a blog post
   */
  toggleLike(blogId: number): Observable<LikeResponse> {
    const body: ToggleLikeRequest = { blogId };
    return this.http
      .post<ApiResponse<LikeResponse>>(`${this.likesUrl}/toggle`, body)
      .pipe(map((res) => res.data));
  }

  /**
   * Toggle like on a comment
   */
  toggleCommentLike(commentId: number): Observable<CommentLikeResponse> {
    const body: ToggleCommentLikeRequest = { commentId };
    return this.http
      .post<ApiResponse<CommentLikeResponse>>(`${this.likesUrl}/comment/toggle`, body)
      .pipe(map((res) => res.data));
  }

  /**
   * Toggle bookmark on a blog post
   */
  toggleBookmark(blogId: number): Observable<BookmarkResponse> {
    const body: ToggleBookmarkRequest = { blogId };
    return this.http
      .post<ApiResponse<BookmarkResponse>>(`${this.bookmarksUrl}/toggle`, body)
      .pipe(map((res) => res.data));
  }

  /**
   * Get user's saved/bookmarked blogs
   */
  getMyBookmarks(page = 0, size = 10): Observable<PageResponse<BlogResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http
      .get<ApiResponse<PageResponse<BlogResponse>>>(`${this.bookmarksUrl}/me`, { params })
      .pipe(map((res) => res.data));
  }

  /**
   * Record a share event
   */
  shareBlog(blogId: number, provider: string): Observable<ShareResponse> {
    const body: CreateShareRequest = { blogId, provider };
    return this.http
      .post<ApiResponse<ShareResponse>>(this.sharesUrl, body)
      .pipe(map((res) => res.data));
  }

  /**
   * Get comments for a blog with pagination
   */
  getComments(blogId: number, page = 0, size = 10): Observable<PageResponse<CommentResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http
      .get<ApiResponse<PageResponse<CommentResponse>>>(`${this.commentsUrl}/blog/${blogId}`, { params })
      .pipe(map((res) => res.data));
  }

  /**
   * Create a new comment (or reply if parentId is specified)
   */
  createComment(request: CreateCommentRequest): Observable<CommentResponse> {
    return this.http
      .post<ApiResponse<CommentResponse>>(this.commentsUrl, request)
      .pipe(map((res) => res.data));
  }

  /**
   * Update comment content
   */
  updateComment(id: number, request: UpdateCommentRequest): Observable<CommentResponse> {
    return this.http
      .put<ApiResponse<CommentResponse>>(`${this.commentsUrl}/${id}`, request)
      .pipe(map((res) => res.data));
  }

  /**
   * Delete comment
   */
  deleteComment(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.commentsUrl}/${id}`)
      .pipe(map(() => void 0));
  }
}
