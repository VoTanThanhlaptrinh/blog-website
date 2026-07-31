import { Component, OnInit, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BlogService } from '../../../core/services/blog.service';
import { AuthService } from '../../../core/services/auth.service';
import { BlogResponse, BlogStatus } from '../../../core/models/blog.model';

@Component({
  selector: 'app-profile-drafts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-drafts.component.html'
})
export class ProfileDraftsComponent implements OnInit {
  private readonly blogService = inject(BlogService);
  private readonly authService = inject(AuthService);

  readonly blogs = signal<BlogResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  readonly hasMore = signal<boolean>(true);
  private nextCursor?: number;
  private isLoadingMore = false;

  ngOnInit(): void {
    this.loadDrafts();
  }

  loadDrafts(isLoadMore = false): void {
    if (this.isLoadingMore || (!this.hasMore() && isLoadMore)) return;
    
    this.isLoadingMore = true;
    if (!isLoadMore) {
        this.loading.set(true);
    }

    this.blogService.getMyBlogsCursor({
      status: BlogStatus.PENDING,
      lastId: this.nextCursor,
      limit: 10
    }).subscribe({
      next: (res) => {
        if (isLoadMore) {
          this.blogs.update(current => [...current, ...(res.content || [])]);
        } else {
          this.blogs.set(res.content || []);
        }
        this.hasMore.set(res.hasMore);
        this.nextCursor = res.nextCursor;
        this.loading.set(false);
        this.isLoadingMore = false;
      },
      error: () => {
        this.error.set('Không thể tải bài viết nháp');
        this.loading.set(false);
        this.isLoadingMore = false;
      }
    });
  }

  @HostListener('window:scroll', ['$event'])
  onScroll(): void {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.hasMore() && !this.isLoadingMore) {
        this.loadDrafts(true);
      }
    }
  }

  deleteDraft(blogId: number, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (confirm('Bạn có chắc chắn muốn xóa bản nháp này không?')) {
      this.blogService.deleteBlog(blogId).subscribe({
        next: () => {
          this.blogs.set(this.blogs().filter(b => b.id !== blogId));
        }
      });
    }
  }
}
