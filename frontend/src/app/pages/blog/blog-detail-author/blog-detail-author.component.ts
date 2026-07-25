import { Component, ElementRef, Inject, PLATFORM_ID, ViewChild } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MarkdownComponent } from 'ngx-markdown';

interface TocItem {
  id: string;
  text: string;
  level: number;
}

@Component({
  selector: 'app-blog-detail-author',
  imports: [MarkdownComponent],
  templateUrl: './blog-detail-author.component.html',
  styleUrl: './blog-detail-author.component.scss'
})
export class BlogDetailAuthorComponent {
  @ViewChild('article', { read: ElementRef }) article!: ElementRef<HTMLElement>;

  toc: TocItem[] = [];

  constructor(@Inject(PLATFORM_ID) private platformId: object) {}

  content = `Angular 19 đã chính thức ra mắt, mang theo những cải tiến vượt bậc trong việc quản lý trạng thái và tối ưu hóa hiệu năng. Trọng tâm của phiên bản này chính là **Signals** — một khái niệm thay đổi hoàn toàn cách chúng ta suy nghĩ về tính phản ứng (reactivity) trong Angular.

## Tại sao lại là Signals?

Trong nhiều năm qua, Angular dựa vào \`zone.js\` để phát hiện thay đổi. Mặc dù hiệu quả, nó thường gây ra tình trạng render thừa (over-rendering) và khiến việc tối ưu hiệu năng trở nên khó khăn. Signals cho phép Angular biết chính xác *chỗ nào* dữ liệu thay đổi, từ đó chỉ cập nhật đúng phần UI đó.

\`\`\`ts
// Định nghĩa một Signal đơn giản
const count = signal(0);

// Đọc giá trị
console.log(count());

// Cập nhật giá trị
count.set(count() + 1);
\`\`\`

Việc chuyển đổi sang kiến trúc dựa trên Signals không chỉ giúp ứng dụng chạy nhanh hơn mà còn làm cho code trở nên dễ đọc và dễ bảo trì hơn đáng kể. Các ứng dụng *standalone* giờ đây có thể loại bỏ hoàn toàn \`zone.js\`, giúp giảm kích thước bundle và tăng tốc độ tải trang đầu tiên.

> "Signals không chỉ là một tính năng mới, đó là tương lai của Angular."

## Tối ưu hóa Render

Với Angular 19, bạn có thể tận dụng \`computed\` signals để tạo ra các giá trị phụ thuộc một cách hiệu quả. Angular sẽ tự động theo dõi các phụ thuộc và chỉ tính toán lại khi thực sự cần thiết.

- Hỗ trợ kiến trúc Standalone mặc định.
- Loại bỏ sự phụ thuộc vào Zone.js cho hiệu năng cực cao.
- Hệ thống template mới tối ưu cho Signals.
- Cải thiện SEO và Server-Side Rendering (SSR).`;

  /** Chuyển tiêu đề thành id (bỏ dấu tiếng Việt, thường hóa, thay khoảng trắng bằng -). */
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

  /** Sau khi markdown render xong: gán id cho mỗi heading và dựng mục lục. */
  onMarkdownReady(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const headings = this.article.nativeElement.querySelectorAll('h1, h2, h3');
    this.toc = Array.from(headings).map((el) => {
      const text = el.textContent?.trim() ?? '';
      const id = this.slugify(text);
      el.id = id;
      return { id, text, level: Number(el.tagName.substring(1)) };
    });
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
