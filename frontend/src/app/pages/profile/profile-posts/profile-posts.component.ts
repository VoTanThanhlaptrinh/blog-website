import { Component, OnInit, inject, signal } from '@angular/core';
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

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    this.loading.set(true);
    const currentUser = this.authService.currentUser();
    
    this.blogService.getBlogs({
      userId: currentUser?.id,
      status: BlogStatus.PUBLISHED,
      page: 0,
      size: 20
    }).subscribe({
      next: (page) => {
        this.blogs.set(page.content || []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Không thể tải bài viết');
        this.loading.set(false);
      }
    });
  }
}
