import { Component, OnInit, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BlogService } from '../../../core/services/blog.service';
import { AuthService } from '../../../core/services/auth.service';
import { BlogResponse, BlogStatus } from '../../../core/models/blog.model';

@Component({
  selector: 'app-profile-posts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-posts.component.html'
})
export class ProfilePostsComponent implements OnInit {
  private readonly blogService = inject(BlogService);
  private readonly authService = inject(AuthService);

  readonly blogs = signal<BlogResponse[]>([]);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly currentStatusFilter = signal<BlogStatus | ''>('');
  readonly isDropdownOpen = signal<boolean>(false);
  
  readonly BlogStatus = BlogStatus;
  readonly hasMore = signal<boolean>(true);
  private nextCursor?: number;
  private isLoadingMore = false;

  readonly filterOptions: { label: string; value: BlogStatus | ''; icon: string }[] = [
    { label: 'Tất cả bài viết', value: '', icon: 'apps' },
    { label: 'Đã xuất bản', value: BlogStatus.PUBLISHED, icon: 'check_circle' },
    { label: 'Đang xét duyệt', value: BlogStatus.PENDING, icon: 'schedule' },
    { label: 'Bản nháp', value: BlogStatus.DRAFT, icon: 'edit_note' },
  ];

  ngOnInit(): void {
    this.loadPosts();
  }

  toggleDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.isDropdownOpen.update(v => !v);
  }

  selectFilter(value: BlogStatus | ''): void {
    this.currentStatusFilter.set(value);
    this.isDropdownOpen.set(false);
    this.nextCursor = undefined;
    this.blogs.set([]);
    this.hasMore.set(true);
    this.loadPosts();
  }

  getSelectedOption() {
    return this.filterOptions.find(opt => opt.value === this.currentStatusFilter()) || this.filterOptions[0];
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.isDropdownOpen()) {
      this.isDropdownOpen.set(false);
    }
  }

  loadPosts(isLoadMore = false): void {
    if (this.isLoadingMore || (!this.hasMore() && isLoadMore)) return;
    
    this.isLoadingMore = true;
    if (!isLoadMore) {
        this.loading.set(true);
    }

    const filterStatus = this.currentStatusFilter() || undefined;

    this.blogService.getMyBlogsCursor({
      status: filterStatus,
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
        this.error.set('Không thể tải bài viết');
        this.loading.set(false);
        this.isLoadingMore = false;
      }
    });
  }

  @HostListener('window:scroll', ['$event'])
  onScroll(): void {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.hasMore() && !this.isLoadingMore) {
        this.loadPosts(true);
      }
    }
  }
}
