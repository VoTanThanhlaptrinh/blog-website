import { Component, ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MarkdownComponent } from 'ngx-markdown';

@Component({
  selector: 'app-blog-creation-split',
  imports: [FormsModule, MarkdownComponent],
  templateUrl: './blog-creation-split.component.html',
  styleUrl: './blog-creation-split.component.scss'
})
export class BlogCreationSplitComponent {
  @ViewChild('bodyInput') bodyInput!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('imageInput') imageInput!: ElementRef<HTMLInputElement>;

  title = '';
  body = '';
  tags: string[] = ['Công nghệ', 'Lập trình'];
  newTag = '';
  showTagInput = false;

  /** Ảnh base64 lưu riêng theo key ngắn (img-N) để textarea không bị nhồi data URL. */
  private images = new Map<string, string>();
  private imageCounter = 0;

  /** History stack cho undo/redo — quản lý cả gõ tay lẫn thao tác toolbar. */
  private history: string[] = [''];
  private historyIndex = 0;
  private readonly historyLimit = 100;
  private typingTimer: ReturnType<typeof setTimeout> | undefined;

  defaultPreview = `Đây là giao diện xem trước cho bài viết của bạn. Khi bạn nhập nội dung ở cột bên trái, các định dạng sẽ được áp dụng trực tiếp tại đây theo phong cách "Editorial Modernist".

Sự cân bằng giữa khoảng trắng (white-space) và kiểu chữ (typography) là ưu tiên hàng đầu của BBlogHub. Chúng tôi sử dụng font Literata cho tiêu đề và Hanken Grotesk cho nội dung để đảm bảo trải nghiệm đọc tối ưu.

> "Thiết kế tốt không chỉ là những gì mắt thấy, mà là cách người đọc cảm nhận sự rõ ràng trong tư duy của người viết."

Hãy bắt đầu hành trình viết lách của bạn ngay hôm nay. Mỗi bài viết là một dấu ấn trí tuệ được lưu lại mãi mãi.`;

  get wordCount(): number {
    const trimmed = this.body.trim();
    return trimmed ? trimmed.split(/\s+/).length : 0;
  }

  /** Chuỗi truyền cho preview: thay mọi placeholder (img-N) bằng data URL thật. */
  get previewData(): string {
    return this.body.replace(/\((img-\d+)\)/g, (match, key) => {
      const url = this.images.get(key);
      return url ? `(${url})` : match;
    });
  }

  get canUndo(): boolean {
    return this.historyIndex > 0;
  }

  get canRedo(): boolean {
    return this.historyIndex < this.history.length - 1;
  }

  autoGrow(event: Event): void {
    const el = event.target as HTMLTextAreaElement;
    el.style.height = '';
    el.style.height = `${el.scrollHeight}px`;
  }

  /** Gõ tay: cập nhật body ngay, ghi lịch sử sau ~300ms để gộp cụm gõ liên tục. */
  onBodyChange(value: string): void {
    this.body = value;
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => this.recordHistory(value), 300);
  }

  /** Đẩy một mốc mới vào history, cắt bỏ nhánh redo nếu đang ở giữa stack. */
  private recordHistory(value: string): void {
    if (value === this.history[this.historyIndex]) {
      return;
    }
    this.history.splice(this.historyIndex + 1);
    this.history.push(value);
    if (this.history.length > this.historyLimit) {
      this.history.shift();
    }
    this.historyIndex = this.history.length - 1;
  }

  undo(): void {
    clearTimeout(this.typingTimer);
    if (!this.canUndo) {
      return;
    }
    this.historyIndex -= 1;
    this.body = this.history[this.historyIndex];
  }

  redo(): void {
    clearTimeout(this.typingTimer);
    if (!this.canRedo) {
      return;
    }
    this.historyIndex += 1;
    this.body = this.history[this.historyIndex];
  }

  onKeydown(event: KeyboardEvent): void {
    const ctrl = event.ctrlKey || event.metaKey;
    if (!ctrl) {
      return;
    }
    const key = event.key.toLowerCase();
    if (key === 'z' && !event.shiftKey) {
      event.preventDefault();
      this.undo();
    } else if (key === 'y' || (key === 'z' && event.shiftKey)) {
      event.preventDefault();
      this.redo();
    }
  }

  /** Bọc vùng chọn bằng prefix/suffix (định dạng inline như bold, italic, code, link). */
  private wrapSelection(prefix: string, suffix: string, placeholder: string): void {
    const el = this.bodyInput.nativeElement;
    const start = el.selectionStart;
    const end = el.selectionEnd;
    const selected = this.body.slice(start, end) || placeholder;
    const before = this.body.slice(0, start);
    const after = this.body.slice(end);

    this.body = `${before}${prefix}${selected}${suffix}${after}`;
    this.recordHistory(this.body);

    const cursorStart = start + prefix.length;
    const cursorEnd = cursorStart + selected.length;
    this.restoreSelection(cursorStart, cursorEnd);
  }

  /** Thêm prefix vào đầu mỗi dòng đang chọn (heading, quote, list). */
  private prefixLines(linePrefix: string): void {
    const el = this.bodyInput.nativeElement;
    const start = el.selectionStart;
    const end = el.selectionEnd;

    const lineStart = this.body.lastIndexOf('\n', start - 1) + 1;
    const before = this.body.slice(0, lineStart);
    const block = this.body.slice(lineStart, end) || '';
    const after = this.body.slice(end);

    const transformed = block
      .split('\n')
      .map((line) => `${linePrefix}${line}`)
      .join('\n');

    this.body = `${before}${transformed}${after}`;
    this.recordHistory(this.body);

    const added = transformed.length - block.length;
    this.restoreSelection(lineStart, end + added);
  }

  private restoreSelection(start: number, end: number): void {
    setTimeout(() => {
      const el = this.bodyInput.nativeElement;
      el.focus();
      el.setSelectionRange(start, end);
    });
  }

  bold(): void {
    this.wrapSelection('**', '**', 'văn bản in đậm');
  }

  italic(): void {
    this.wrapSelection('*', '*', 'văn bản in nghiêng');
  }

  inlineCode(): void {
    this.wrapSelection('`', '`', 'code');
  }

  codeBlock(): void {
    this.wrapSelection('\n```ts\n', '\n```\n', 'code');
  }

  link(): void {
    this.wrapSelection('[', '](https://)', 'liên kết');
  }

  heading(): void {
    this.prefixLines('# ');
  }

  blockquote(): void {
    this.prefixLines('> ');
  }

  bulletList(): void {
    this.prefixLines('- ');
  }

  openImagePicker(): void {
    this.imageInput.nativeElement.click();
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const url = reader.result as string;
      this.imageCounter += 1;
      const key = `img-${this.imageCounter}`;
      this.images.set(key, url);
      this.body = `${this.body}\n![${file.name}](${key})\n`;
      this.recordHistory(this.body);
    };
    reader.readAsDataURL(file);
    input.value = '';
  }

  addTag(): void {
    const tag = this.newTag.trim();
    if (tag && !this.tags.includes(tag)) {
      this.tags.push(tag);
    }
    this.newTag = '';
    this.showTagInput = false;
  }

  removeTag(index: number): void {
    this.tags.splice(index, 1);
  }
}
