import { afterNextRender, Component, OnInit, inject, signal, PLATFORM_ID } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { BlogCardComponent } from '../../shared/components/blog-card/blog-card.component';
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
  imports: [CommonModule, RouterLink, BlogCardComponent],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private readonly blogService = inject(BlogService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly rotatingWords = ['kết nối cộng đồng', 'lan tỏa ý tưởng', 'phát triển bản thân', 'truyền cảm hứng'];
  readonly currentWord = signal(0);

  readonly stats: Stat[] = [
    { value: 0, suffix: '+', label: 'Bài viết', display: signal(0) },
    { value: 0, suffix: '+', label: 'Tác giả', display: signal(0) },
    { value: 0, suffix: '+', label: 'Lượt yêu thích', display: signal(0) },
    { value: 0, suffix: '+', label: 'Chủ đề', display: signal(0) },
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
    });
  }

  ngOnInit(): void {
    this.loadBlogs(this.currentPage());
    this.loadStats();
  }

  loadStats(): void {
    this.blogService.getHomeStats().subscribe((res) => {
      this.stats[0].value = res.totalBlogs;
      this.stats[0].suffix = res.totalBlogs >= 1000 ? 'K+' : '+';

      this.stats[1].value = res.totalAuthors;
      this.stats[1].suffix = res.totalAuthors >= 1000 ? 'K+' : '+';

      this.stats[2].value = res.totalLikes;
      this.stats[2].suffix = res.totalLikes >= 1000 ? 'K+' : '+';

      this.stats[3].value = res.totalCategories;
      this.stats[3].suffix = res.totalCategories >= 1000 ? 'K+' : '+';

      this.startCountUp();
    });
  }

  loadBlogs(page: number): void {
    this.currentPage.set(page);
    this.blogService.getBlogs({
      page: page,
      size: this.pageSize(),
    }).subscribe();
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
    if (!isPlatformBrowser(this.platformId)) {
      for (const stat of this.stats) {
        stat.display.set(stat.value);
      }
      return;
    }
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
