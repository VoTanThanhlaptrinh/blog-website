import { Component, ElementRef, Inject, OnInit, PLATFORM_ID, ViewChild, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MarkdownComponent } from 'ngx-markdown';
import { BlogService } from '../../../core/services/blog.service';
import { BlogResponse } from '../../../core/models/blog.model';

interface TocItem {
  id: string;
  text: string;
  level: number;
}

@Component({
  selector: 'app-blog-detail-author',
  standalone: true,
  imports: [CommonModule, MarkdownComponent],
  templateUrl: './blog-detail-author.component.html',
  styleUrl: './blog-detail-author.component.scss'
})
export class BlogDetailAuthorComponent implements OnInit {
  @ViewChild('article', { read: ElementRef }) article!: ElementRef<HTMLElement>;

  private readonly route = inject(ActivatedRoute);
  private readonly blogService = inject(BlogService);

  readonly blog = signal<BlogResponse | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  toc: TocItem[] = [];

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.fetchBlogDetail(id);
      } else {
        // Fallback default sample if no ID provided in route
        this.loading.set(false);
      }
    });
  }

  fetchBlogDetail(id: string | number): void {
    this.loading.set(true);
    this.error.set(null);
    this.blogService.getBlogById(id).subscribe({
      next: (res) => {
        this.blog.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Không thể lấy nội dung bài viết này.');
        this.loading.set(false);
      }
    });
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
