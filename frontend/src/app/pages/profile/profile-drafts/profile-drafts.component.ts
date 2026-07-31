import { Component, OnInit, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BlogService } from '../../../core/services/blog.service';
import { AuthService } from '../../../core/services/auth.service';
import { BlogResponse, BlogStatus } from '../../../core/models/blog.model';
import { finalize } from 'rxjs';
import { ConfirmService } from '../../../core/services/confirm.service';

@Component({
  selector: 'app-profile-drafts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-drafts.component.html'
})
export class ProfileDraftsComponent implements OnInit {
  private readonly blogService = inject(BlogService);
  private readonly authService = inject(AuthService);
  private readonly confirmService = inject(ConfirmService);

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
    })
    .pipe(finalize(() => {
      this.loading.set(false);
      this.isLoadingMore = false;
    }))
    .subscribe((res) => {
      if (isLoadMore) {
        this.blogs.update(current => [...current, ...(res.content || [])]);
      } else {
        this.blogs.set(res.content || []);
      }
      this.hasMore.set(res.hasMore);
      this.nextCursor = res.nextCursor;
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

  async deleteDraft(blogId: number, event: Event): Promise<void> {
    event.stopPropagation();
    event.preventDefault();
    const confirmed = await this.confirmService.confirm({
      title: 'Xóa bản nháp',
      message: 'Bạn có chắc chắn muốn xóa bản nháp này không? Hành động này không thể hoàn tác.',
      confirmText: 'Xóa bài',
      actionType: 'danger'
    });

    if (confirmed) {
      this.blogService.deleteBlog(blogId).subscribe(() => {
        this.blogs.set(this.blogs().filter(b => b.id !== blogId));
      });
    }
  }
}
