import { Component, ElementRef, HostListener, OnInit, PLATFORM_ID, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MarkdownComponent } from 'ngx-markdown';
import { FileService } from '../../../core/services/file.service';
import { BlogService } from '../../../core/services/blog.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { BlogStatus } from '../../../core/models/blog.model';
import { BlogCardComponent } from '../../../shared/components/blog-card/blog-card.component';
import { isPlatformServer } from '@angular/common';

interface HistoryState {
  content: string;
  selectionStart: number;
  selectionEnd: number;
}

@Component({
  selector: 'app-blog-creation-split',
  imports: [FormsModule, MarkdownComponent, RouterLink, BlogCardComponent],
  templateUrl: './blog-creation-split.component.html',
  styleUrl: './blog-creation-split.component.scss'
})
export class BlogCreationSplitComponent implements OnInit {
  private readonly fileService = inject(FileService);
  private readonly blogService = inject(BlogService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly todayDate = new Date().toLocaleDateString('vi-VN');

  @ViewChild('bodyInput') bodyInput!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('imageInput') imageInput!: ElementRef<HTMLInputElement>;

  title = '';
  body = '';
  tags: string[] = ['Công nghệ', 'Lập trình'];
  newTag = '';
  showTagInput = false;
  mobilePreview = signal(false);
  submitting = signal(false);

  thumbnailUrl = signal<string>('');
  thumbnailUploading = signal(false);
  previewMode = signal<'article' | 'card'>('article');

  isThumbnailLibraryOpen = signal(false);
  usedThumbnails = signal<string[]>([]);
  isLoadingThumbnails = signal(false);
  selectedThumbnailFromLib = signal<string>('');

  isEditMode = false;
  blogId: number | string | null = null;
  BlogStatus = BlogStatus;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && isPlatformServer(PLATFORM_ID)) {
      this.isEditMode = true;
      this.blogId = id;
      this.blogService.getBlogById(id).subscribe({
        next: (blog) => {
          this.title = blog.title;
          this.body = blog.content;
          if (blog.thumbnailUrl) {
            this.thumbnailUrl.set(blog.thumbnailUrl);
          }
          this.recordHistory(this.body);
        },
        error: (err) => {
          console.error('Lỗi khi tải thông tin bài viết:', err);
          this.toastService.error('Không thể tải bài viết để chỉnh sửa.');
        }
      });
    }
  }

  /** Ảnh base64 lưu riêng theo key ngắn (img-N) để textarea không bị nhồi data URL. */
  private images = new Map<string, string>();
  private imageCounter = 0;

  /** History stack cho undo/redo — quản lý cả gõ tay lẫn thao tác toolbar, kèm vị trí con trỏ. */
  private history: HistoryState[] = [{ content: '', selectionStart: 0, selectionEnd: 0 }];
  private historyIndex = 0;
  private readonly historyLimit = 100;
  private typingTimer: ReturnType<typeof setTimeout> | undefined;

  defaultPreview = `Đây là giao diện xem trước cho bài viết của bạn. Khi bạn nhập nội dung ở cột bên trái, các định dạng sẽ được áp dụng trực tiếp tại đây theo phong cách "Editorial Modernist".

Sự cân bằng giữa khoảng trắng (white-space) và kiểu chữ (typography) là ưu tiên hàng đầu của BlogHub. Chúng tôi sử dụng font Literata cho tiêu đề và Hanken Grotesk cho nội dung để đảm bảo trải nghiệm đọc tối ưu.

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
    return this.historyIndex > 0 || (this.history[this.historyIndex] && this.body !== this.history[this.historyIndex].content);
  }

  get canRedo(): boolean {
    return this.historyIndex < this.history.length - 1;
  }

  get mockBlogCard(): any {
    const currentUser = this.authService.currentUser();
    return {
      id: 0,
      title: this.title || 'Tiêu đề bài viết...',
      description: this.body ? this.body.substring(0, 150) : 'Mô tả bài viết sẽ hiển thị ở đây...',
      thumbnailUrl: this.thumbnailUrl() || null,
      content: this.body,
      status: BlogStatus.DRAFT,
      author: {
        id: currentUser?.id || 0,
        email: currentUser?.email || 'user@example.com',
        avatarUrl: currentUser?.avatarUrl || null
      },
      category: {
        id: 0,
        name: this.tags.length > 0 ? this.tags[0] : 'Công nghệ',
        slug: 'cong-nghe'
      },
      likesCount: 0,
      commentsCount: 0,
      viewsCount: 0,
      sharesCount: 0,
      createdDate: new Date().toISOString(),
      modifiedDate: new Date().toISOString()
    };
  }

  autoGrow(event: Event): void {
    const el = event.target as HTMLTextAreaElement;
    el.style.height = '';
    el.style.height = `${el.scrollHeight}px`;
  }

  private getCursor(): { start: number; end: number } {
    const el = this.bodyInput?.nativeElement;
    return el ? { start: el.selectionStart, end: el.selectionEnd } : { start: 0, end: 0 };
  }

  /** Gõ tay: cập nhật body ngay, ghi lịch sử sau ~300ms để gộp cụm gõ liên tục. */
  onBodyChange(value: string): void {
    this.body = value;
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      const { start, end } = this.getCursor();
      this.recordHistory(value, start, end);
    }, 300);
  }

  /** Đẩy một mốc mới vào history, cắt bỏ nhánh redo nếu đang ở giữa stack. */
  private recordHistory(value: string, start?: number, end?: number): void {
    const current = this.history[this.historyIndex];
    if (value === current?.content) {
      return;
    }
    const cursor = start !== undefined && end !== undefined ? { start, end } : this.getCursor();
    this.history.splice(this.historyIndex + 1);
    this.history.push({ content: value, selectionStart: cursor.start, selectionEnd: cursor.end });
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
    const currentCheckpoint = this.history[this.historyIndex];
    if (this.body !== currentCheckpoint.content) {
      this.body = currentCheckpoint.content;
      this.restoreSelection(currentCheckpoint.selectionStart, currentCheckpoint.selectionEnd);
      return;
    }
    this.historyIndex -= 1;
    const target = this.history[this.historyIndex];
    this.body = target.content;
    this.restoreSelection(target.selectionStart, target.selectionEnd);
  }

  redo(): void {
    clearTimeout(this.typingTimer);
    if (!this.canRedo) {
      return;
    }
    this.historyIndex += 1;
    const target = this.history[this.historyIndex];
    this.body = target.content;
    this.restoreSelection(target.selectionStart, target.selectionEnd);
  }

  @HostListener('window:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const ctrl = event.ctrlKey || event.metaKey;
    if (!ctrl) {
      return;
    }
    const target = event.target as HTMLElement;
    if (target && target !== this.bodyInput?.nativeElement && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
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
    const cursorStart = start + prefix.length;
    const cursorEnd = cursorStart + selected.length;
    this.recordHistory(this.body, cursorStart, cursorEnd);
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
    const added = transformed.length - block.length;
    const cursorEnd = end + added;
    this.recordHistory(this.body, lineStart, cursorEnd);
    this.restoreSelection(lineStart, cursorEnd);
  }

  private restoreSelection(start: number, end: number): void {
    setTimeout(() => {
      const el = this.bodyInput?.nativeElement;
      if (el) {
        el.focus();
        el.setSelectionRange(start, end);
      }
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

  heading(level: number): void {
    const prefix = '#'.repeat(level) + ' ';
    this.prefixLines(prefix);
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
    const tempKey = `[Đang tải ảnh "${file.name}" lên...]`;
    this.body = `${this.body}\n![${file.name}](${tempKey})\n`;
    this.recordHistory(this.body);

    this.fileService.uploadFileToR2(file, 'blog/temp').subscribe({
      next: (publicUrl: string) => {
        this.body = this.body.replace(`(${tempKey})`, `(${publicUrl})`);
        this.recordHistory(this.body);
      },
      error: (err: unknown) => {
        console.error('Lỗi upload ảnh lên Cloudflare R2:', err);
        this.toastService.error(`Không thể upload ảnh "${file.name}". Vui lòng thử lại.`);
        this.body = this.body.replace(`\n![${file.name}](${tempKey})\n`, '');
        this.recordHistory(this.body);
      },
    });

    input.value = '';
  }

  onThumbnailSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    this.thumbnailUploading.set(true);
    this.fileService.uploadFileToR2(file, 'blog/thumbnails').subscribe({
      next: (url) => {
        this.thumbnailUrl.set(url);
        this.thumbnailUploading.set(false);
      },
      error: (err) => {
        console.error('Lỗi upload ảnh bìa:', err);
        this.toastService.error('Upload ảnh bìa thất bại.');
        this.thumbnailUploading.set(false);
      }
    });
  }

  openThumbnailLibrary(): void {
    this.isThumbnailLibraryOpen.set(true);
    this.selectedThumbnailFromLib.set(this.thumbnailUrl());
    this.isLoadingThumbnails.set(true);
    this.blogService.getMyUsedThumbnails().subscribe({
      next: (urls) => {
        this.usedThumbnails.set(urls || []);
        this.isLoadingThumbnails.set(false);
      },
      error: (err) => {
        console.error('Lỗi lấy danh sách ảnh bìa:', err);
        this.toastService.error('Không thể lấy thư viện ảnh bìa.');
        this.isLoadingThumbnails.set(false);
      }
    });
  }

  confirmThumbnailSelection(): void {
    if (this.selectedThumbnailFromLib()) {
      this.thumbnailUrl.set(this.selectedThumbnailFromLib());
    }
    this.isThumbnailLibraryOpen.set(false);
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

  toggleMobilePreview(): void {
    this.mobilePreview.set(!this.mobilePreview());
  }

  submit(status: BlogStatus): void {
    if (!this.title.trim()) {
      this.toastService.warning('Vui lòng nhập tiêu đề bài viết.');
      return;
    }
    if (!this.body.trim()) {
      this.toastService.warning('Vui lòng nhập nội dung bài viết.');
      return;
    }

    const description = this.body.trim().slice(0, 150);
    this.submitting.set(true);

    if (this.isEditMode && this.blogId) {
      this.blogService.updateBlog(this.blogId, {
        title: this.title.trim(),
        description,
        content: this.body,
        status,
        thumbnailUrl: this.thumbnailUrl() || undefined
      }).subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.router.navigate(['/blog/detail', res.id || this.blogId]);
        },
        error: (err) => {
          console.error('Lỗi khi cập nhật bài viết:', err);
          this.submitting.set(false);
          this.toastService.error(err?.error?.message || 'Có lỗi xảy ra khi cập nhật bài viết.');
        }
      });
    } else {
      this.blogService.createBlog({
        title: this.title.trim(),
        description,
        content: this.body,
        status,
        thumbnailUrl: this.thumbnailUrl() || undefined
      }).subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.router.navigate(['/blog/detail', res.id]);
        },
        error: (err) => {
          console.error('Lỗi khi tạo bài viết:', err);
          this.submitting.set(false);
          this.toastService.error(err?.error?.message || 'Có lỗi xảy ra khi tạo bài viết.');
        }
      });
    }
  }
}


