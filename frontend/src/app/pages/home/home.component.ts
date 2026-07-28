import { afterNextRender, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BlogService } from '../../core/services/blog.service';
import { BlogResponse, PageResponse } from '../../core/models/blog.model';

interface Stat {
  value: number;
  suffix: string;
  label: string;
  display: ReturnType<typeof signal<number>>;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private readonly blogService = inject(BlogService);

  readonly rotatingWords = ['kết nối cộng đồng', 'lan tỏa ý tưởng', 'phát triển bản thân', 'truyền cảm hứng'];
  readonly currentWord = signal(0);

  readonly stats: Stat[] = [
    { value: 12000, suffix: 'K+', label: 'Bài viết', display: signal(0) },
    { value: 4800, suffix: 'K+', label: 'Tác giả', display: signal(0) },
    { value: 95000, suffix: 'K+', label: 'Lượt yêu thích', display: signal(0) },
    { value: 30, suffix: '+', label: 'Chủ đề', display: signal(0) },
  ];

  // Observables / Signals for Blog Data
  readonly blogs$ = this.blogService.blogs$;
  readonly pageMeta$ = this.blogService.pageMeta$;
  readonly loading$ = this.blogService.loading$;
  readonly error$ = this.blogService.error$;

  readonly currentPage = signal<number>(0);
  readonly pageSize = signal<number>(6);

  constructor() {
    afterNextRender(() => {
      this.startWordRotation();
      this.startCountUp();
    });
  }

  ngOnInit(): void {
    this.loadBlogs(this.currentPage());
  }

  loadBlogs(page: number): void {
    this.currentPage.set(page);
    this.blogService.getBlogs({
      page: page,
      size: this.pageSize(),
    }).subscribe();
  }

  onPageChange(page: number): void {
    this.loadBlogs(page);
    window.scrollTo({ top: 500, behavior: 'smooth' });
  }

  getPagesArray(totalPages: number): number[] {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  formatStat(stat: Stat): string {
    const v = stat.display();
    if (stat.value >= 1000) {
      return (v / 1000).toFixed(v >= stat.value ? 0 : 1) + stat.suffix;
    }
    return v + stat.suffix;
  }

  private startWordRotation() {
    setInterval(() => {
      this.currentWord.update((i) => (i + 1) % this.rotatingWords.length);
    }, 2800);
  }

  private startCountUp() {
    const duration = 1600;
    const start = performance.now();
    const tick = (now: number) => {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      for (const stat of this.stats) {
        stat.display.set(Math.round(stat.value * eased));
      }
      if (progress < 1) {
        requestAnimationFrame(tick);
      }
    };
    requestAnimationFrame(tick);
  }

  readonly topics = [
    'Angular',
    'Spring Boot',
    'CSS',
    'Database',
    'Security',
    'DevOps',
    'TypeScript',
    'System Design',
  ];
}
