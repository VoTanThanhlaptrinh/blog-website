import { Component, OnInit, inject, signal } from '@angular/core';
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

  ngOnInit(): void {
    this.loadDrafts();
  }

  loadDrafts(): void {
    this.loading.set(true);
    const currentUser = this.authService.currentUser();

    this.blogService.getBlogs({
      userId: currentUser?.id,
      status: BlogStatus.DRAFT,
      page: 0,
      size: 20
    }).subscribe({
      next: (page) => {
        this.blogs.set(page.content || []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Không thể tải bài viết nháp');
        this.loading.set(false);
      }
    });
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
