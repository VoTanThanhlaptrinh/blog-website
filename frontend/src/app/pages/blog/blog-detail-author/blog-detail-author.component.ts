import { Component, ElementRef, Inject, OnInit, OnDestroy, PLATFORM_ID, ViewChild, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MarkdownComponent } from 'ngx-markdown';
import { BlogService } from '../../../core/services/blog.service';
import { InteractionService } from '../../../core/services/interaction.service';
import { AuthService } from '../../../core/services/auth.service';
import { BlogResponse } from '../../../core/models/blog.model';
import { CommentResponse } from '../../../core/models/interaction.model';
import { CommentItemComponent } from '../../../components/comment-item/comment-item.component';

interface TocItem {
  id: string;
  text: string;
  level: number;
}

@Component({
  selector: 'app-blog-detail-author',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownComponent, CommentItemComponent],
  templateUrl: './blog-detail-author.component.html',
  styleUrl: './blog-detail-author.component.scss',
})
export class BlogDetailAuthorComponent implements OnInit, OnDestroy {
  @ViewChild('article', { read: ElementRef }) article!: ElementRef<HTMLElement>;

  private readonly route = inject(ActivatedRoute);
  private readonly blogService = inject(BlogService);
  private readonly interactionService = inject(InteractionService);
  readonly authService = inject(AuthService);

  readonly blog = signal<BlogResponse | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  // Interaction Signals
  readonly isLiked = signal<boolean>(false);
  readonly likesCount = signal<number>(0);
  readonly isBookmarked = signal<boolean>(false);
  readonly sharesCount = signal<number>(0);
  readonly viewsCount = signal<number>(0);

  // Comments Signals
  readonly comments = signal<CommentResponse[]>([]);
  readonly commentsLoading = signal<boolean>(false);
  readonly commentsPage = signal<number>(0);
  readonly hasMoreComments = signal<boolean>(false);
  readonly totalComments = signal<number>(0);

  newCommentContent = signal<string>('');
  replyingTo = signal<{ parentId: number; authorEmail: string } | null>(null);
  showShareModal = signal<boolean>(false);

  toc: TocItem[] = [];
  private viewTimer: any = null;

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  ngOnInit(): void {
    // Attempt to load current user profile if logged in but empty
    if (!this.authService.currentUser()) {
      this.authService.getProfile().subscribe({ error: () => {} });
    }

    this.route.params.subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.fetchBlogDetail(id);
      } else {
        this.loading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.viewTimer) {
      clearTimeout(this.viewTimer);
    }
  }

  fetchBlogDetail(id: string | number): void {
    this.loading.set(true);
    this.error.set(null);
    this.blogService.getBlogById(id).subscribe({
      next: (res) => {
        this.blog.set(res);
        this.likesCount.set(res.likesCount || 0);
        this.sharesCount.set(res.sharesCount || 0);
        this.viewsCount.set(res.viewsCount || 0);
        this.totalComments.set(res.commentsCount || 0);
        this.loading.set(false);

        // Schedule View Recording (after 5 seconds on page)
        this.scheduleViewRecord(res.id);

        // Fetch initial page of comments
        this.fetchComments(res.id, 0);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Không thể lấy nội dung bài viết này.');
        this.loading.set(false);
      },
    });
  }

  /** View Recording */
  private scheduleViewRecord(blogId: number): void {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.viewTimer) clearTimeout(this.viewTimer);

    this.viewTimer = setTimeout(() => {
      this.interactionService.recordView(blogId).subscribe({
        next: (res) => {
          if (res && res.viewsCount !== undefined) {
            this.viewsCount.set(res.viewsCount);
          } else {
            this.viewsCount.update((v) => v + 1);
          }
        },
        error: () => {},
      });
    }, 5000);
  }

  /** Optimistic UI for Toggle Like */
  toggleLike(): void {
    const b = this.blog();
    if (!b) return;

    const prevLiked = this.isLiked();
    const prevCount = this.likesCount();

    // Optimistic Update
    const nextLiked = !prevLiked;
    this.isLiked.set(nextLiked);
    this.likesCount.set(nextLiked ? prevCount + 1 : Math.max(0, prevCount - 1));

    this.interactionService.toggleLike(b.id).subscribe({
      next: (res) => {
        this.isLiked.set(res.liked);
        this.likesCount.set(res.likesCount);
      },
      error: () => {
        // Rollback on error
        this.isLiked.set(prevLiked);
        this.likesCount.set(prevCount);
      },
    });
  }

  /** Optimistic UI for Toggle Bookmark */
  toggleBookmark(): void {
    const b = this.blog();
    if (!b) return;

    const prevBookmarked = this.isBookmarked();
    this.isBookmarked.set(!prevBookmarked);

    this.interactionService.toggleBookmark(b.id).subscribe({
      next: (res) => {
        this.isBookmarked.set(res.bookmarked);
      },
      error: () => {
        this.isBookmarked.set(prevBookmarked);
      },
    });
  }

  /** Handle Share */
  sharePost(provider: string): void {
    const b = this.blog();
    if (!b) return;

    this.interactionService.shareBlog(b.id, provider).subscribe({
      next: (res) => {
        this.sharesCount.set(res.sharesCount);
      },
      error: () => {},
    });

    if (isPlatformBrowser(this.platformId)) {
      const url = window.location.href;
      if (provider === 'facebook') {
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`, '_blank');
      } else if (provider === 'twitter') {
        window.open(`https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(b.title)}`, '_blank');
      } else if (provider === 'link') {
        navigator.clipboard.writeText(url);
        alert('Đã sao chép liên kết bài viết!');
      }
    }
    this.showShareModal.set(false);
  }

  toggleShareModal(): void {
    this.showShareModal.update((v) => !v);
  }

  /** Load Comments */
  fetchComments(blogId: number, page: number): void {
    this.commentsLoading.set(true);
    this.interactionService.getComments(blogId, page, 10).subscribe({
      next: (res) => {
        if (page === 0) {
          this.comments.set(res.content || []);
        } else {
          this.comments.update((existing) => [...existing, ...(res.content || [])]);
        }
        this.commentsPage.set(res.pageNumber);
        this.hasMoreComments.set(!res.last);
        this.commentsLoading.set(false);
      },
      error: () => {
        this.commentsLoading.set(false);
      },
    });
  }

  loadMoreComments(): void {
    const b = this.blog();
    if (!b || this.commentsLoading() || !this.hasMoreComments()) return;
    this.fetchComments(b.id, this.commentsPage() + 1);
  }

  /** Post Comment */
  submitComment(): void {
    const b = this.blog();
    const content = this.newCommentContent().trim();
    if (!b || !content) return;

    const parent = this.replyingTo();
    const req = {
      blogId: b.id,
      content,
      parentId: parent ? parent.parentId : null,
    };

    this.interactionService.createComment(req).subscribe({
      next: (newComment) => {
        this.newCommentContent.set('');
        this.replyingTo.set(null);
        this.totalComments.update((c) => c + 1);

        if (req.parentId) {
          // Add to nested replies in local state
          this.addReplyToCommentTree(this.comments(), req.parentId, newComment);
          this.comments.update((list) => [...list]);
        } else {
          // Prepend to top-level comments
          this.comments.update((list) => [newComment, ...list]);
        }
      },
      error: (err) => {
        alert(err?.error?.message || 'Gửi bình luận thất bại');
      },
    });
  }

  cancelReply(): void {
    this.replyingTo.set(null);
  }

  setReplyTo(event: { parentId: number; authorEmail: string }): void {
    this.replyingTo.set(event);
  }

  /** Comment Interactions */
  handleLikeComment(commentId: number): void {
    // Optimistic toggle in memory
    this.toggleCommentLikeInTree(this.comments(), commentId);
    this.comments.update((list) => [...list]);

    this.interactionService.toggleCommentLike(commentId).subscribe({
      next: (res) => {
        this.updateCommentLikeInTree(this.comments(), commentId, res.liked, res.likesCount);
        this.comments.update((list) => [...list]);
      },
      error: () => {},
    });
  }

  handleEditComment(event: { commentId: number; content: string }): void {
    this.interactionService.updateComment(event.commentId, { content: event.content }).subscribe({
      next: (updated) => {
        this.updateCommentContentInTree(this.comments(), event.commentId, updated.content);
        this.comments.update((list) => [...list]);
      },
      error: (err) => {
        alert(err?.error?.message || 'Cập nhật bình luận thất bại');
      },
    });
  }

  handleDeleteComment(commentId: number): void {
    this.interactionService.deleteComment(commentId).subscribe({
      next: () => {
        this.removeCommentFromTree(this.comments(), commentId);
        this.comments.update((list) => [...list]);
        this.totalComments.update((c) => Math.max(0, c - 1));
      },
      error: (err) => {
        alert(err?.error?.message || 'Xóa bình luận thất bại');
      },
    });
  }

  // Recursive Helper Functions for Comment Tree Mutation
  private addReplyToCommentTree(tree: CommentResponse[], parentId: number, newReply: CommentResponse): boolean {
    for (const item of tree) {
      if (item.id === parentId) {
        if (!item.replies) item.replies = [];
        item.replies.push(newReply);
        return true;
      }
      if (item.replies && item.replies.length > 0) {
        if (this.addReplyToCommentTree(item.replies, parentId, newReply)) return true;
      }
    }
    return false;
  }

  private toggleCommentLikeInTree(tree: CommentResponse[], commentId: number): boolean {
    for (const item of tree) {
      if (item.id === commentId) {
        item.likedByCurrentUser = !item.likedByCurrentUser;
        item.likeCount = item.likedByCurrentUser ? (item.likeCount || 0) + 1 : Math.max(0, (item.likeCount || 0) - 1);
        return true;
      }
      if (item.replies && item.replies.length > 0) {
        if (this.toggleCommentLikeInTree(item.replies, commentId)) return true;
      }
    }
    return false;
  }

  private updateCommentLikeInTree(tree: CommentResponse[], commentId: number, liked: boolean, count: number): boolean {
    for (const item of tree) {
      if (item.id === commentId) {
        item.likedByCurrentUser = liked;
        item.likeCount = count;
        return true;
      }
      if (item.replies && item.replies.length > 0) {
        if (this.updateCommentLikeInTree(item.replies, commentId, liked, count)) return true;
      }
    }
    return false;
  }

  private updateCommentContentInTree(tree: CommentResponse[], commentId: number, content: string): boolean {
    for (const item of tree) {
      if (item.id === commentId) {
        item.content = content;
        return true;
      }
      if (item.replies && item.replies.length > 0) {
        if (this.updateCommentContentInTree(item.replies, commentId, content)) return true;
      }
    }
    return false;
  }

  private removeCommentFromTree(tree: CommentResponse[], commentId: number): boolean {
    const idx = tree.findIndex((c) => c.id === commentId);
    if (idx !== -1) {
      tree.splice(idx, 1);
      return true;
    }
    for (const item of tree) {
      if (item.replies && item.replies.length > 0) {
        if (this.removeCommentFromTree(item.replies, commentId)) return true;
      }
    }
    return false;
  }

  /** Convert title to slug id */
  private slugify(text: string): string {
    return text
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/đ/g, 'd')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/\s+/g, '-');
  }

  /** After markdown renders: assign heading ids & build TOC */
  onMarkdownReady(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    setTimeout(() => {
      if (!this.article?.nativeElement) return;
      const headings = this.article.nativeElement.querySelectorAll('h1, h2, h3');
      this.toc = Array.from(headings).map((el) => {
        const text = el.textContent?.trim() ?? '';
        const id = this.slugify(text);
        el.id = id;
        return { id, text, level: Number(el.tagName.substring(1)) };
      });
    }, 100);
  }

  scrollTo(id: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const el = document.getElementById(id);
    if (!el) {
      return;
    }
    const top = el.getBoundingClientRect().top + window.scrollY - 72;
    window.scrollTo({ top, behavior: 'smooth' });
  }
}
