import { afterNextRender, Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Post {
  id: number;
  title: string;
  excerpt: string;
  author: string;
  authorInitial: string;
  date: string;
  readTime: number;
  likes: number;
  comments: number;
  tag: string;
}

interface Stat {
  value: number;
  suffix: string;
  label: string;
  display: ReturnType<typeof signal<number>>;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  readonly rotatingWords = ['kết nối cộng đồng', 'lan tỏa ý tưởng', 'phát triển bản thân', 'truyền cảm hứng'];
  readonly currentWord = signal(0);

  readonly stats: Stat[] = [
    { value: 12000, suffix: 'K+', label: 'Bài viết', display: signal(0) },
    { value: 4800, suffix: 'K+', label: 'Tác giả', display: signal(0) },
    { value: 95000, suffix: 'K+', label: 'Lượt yêu thích', display: signal(0) },
    { value: 30, suffix: '+', label: 'Chủ đề', display: signal(0) },
  ];

  constructor() {
    afterNextRender(() => {
      this.startWordRotation();
      this.startCountUp();
    });
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

  readonly featured: Post = {
    id: 1,
    title: 'Bắt đầu với Angular 19 và Signals: Kiến trúc phản ứng hiện đại',
    excerpt:
      'Signals thay đổi cách chúng ta quản lý trạng thái trong Angular. Bài viết đi sâu vào cách xây dựng ứng dụng standalone, tối ưu render và loại bỏ zone.js một cách an toàn.',
    author: 'Nguyễn Văn A',
    authorInitial: 'A',
    date: '01 Th7, 2026',
    readTime: 8,
    likes: 324,
    comments: 42,
    tag: 'Angular',
  };

  readonly posts: Post[] = [
    {
      id: 2,
      title: 'Thiết kế REST API với Spring Boot theo kiến trúc DDD',
      excerpt:
        'Hướng dẫn xây dựng REST API sạch, bảo mật và dễ mở rộng bằng cách tách domain, application và infrastructure.',
      author: 'Trần Thị B',
      authorInitial: 'B',
      date: '28 Th6, 2026',
      readTime: 8,
      likes: 189,
      comments: 12,
      tag: 'Spring Boot',
    },
    {
      id: 3,
      title: 'Tailwind CSS: Utility-first trong thực tế',
      excerpt:
        'Vì sao utility-first CSS giúp bạn xây dựng giao diện nhanh hơn mà vẫn nhất quán trong dự án lớn.',
      author: 'Lê Văn C',
      authorInitial: 'C',
      date: '20 Th6, 2026',
      readTime: 6,
      likes: 256,
      comments: 24,
      tag: 'CSS',
    },
    {
      id: 4,
      title: 'PostgreSQL: Tối ưu truy vấn cho hệ thống đọc nhiều',
      excerpt:
        'Index, materialized view và cách đọc EXPLAIN ANALYZE để tăng tốc những truy vấn chậm nhất của bạn.',
      author: 'Phạm Thị D',
      authorInitial: 'D',
      date: '15 Th6, 2026',
      readTime: 10,
      likes: 142,
      comments: 9,
      tag: 'Database',
    },
    {
      id: 5,
      title: 'JWT và OAuth2 Resource Server: Bảo mật đúng cách',
      excerpt:
        'Phân biệt access token, refresh token và cách cấu hình Resource Server để tránh những lỗ hổng phổ biến.',
      author: 'Hoàng Văn E',
      authorInitial: 'E',
      date: '10 Th6, 2026',
      readTime: 7,
      likes: 198,
      comments: 15,
      tag: 'Security',
    },
    {
      id: 6,
      title: 'Redis trong thực chiến: Cache, session và rate limit',
      excerpt:
        'Ba mô hình dùng Redis phổ biến nhất, kèm ví dụ cụ thể và những cạm bẫy về TTL cần tránh.',
      author: 'Vũ Thị F',
      authorInitial: 'F',
      date: '05 Th6, 2026',
      readTime: 6,
      likes: 167,
      comments: 11,
      tag: 'Backend',
    },
  ];

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
