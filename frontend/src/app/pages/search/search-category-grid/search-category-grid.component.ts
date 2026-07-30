import { Component, signal, computed, HostListener, OnInit, Inject, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse, BlogResponse, BlogStatus } from '../../../core/models/blog.model';
import { BlogCardComponent } from '../../../shared/components/blog-card/blog-card.component';

export interface SortOption {
  label: string;
  value: string;
  icon: string;
}

export interface InteractionFilterOption {
  key: string;
  label: string;
}

@Component({
  selector: 'app-search-category-grid',
  standalone: true,
  imports: [BlogCardComponent],
  templateUrl: './search-category-grid.component.html',
  styleUrl: './search-category-grid.component.scss'
})
export class SearchCategoryGridComponent implements OnInit {
  filterOpen = signal(false);
  sortOpen = signal(false);
  isDesktop = signal(false);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) { }

  private readonly categoryService = inject(CategoryService);

  ngOnInit() {
    this.checkResponsiveFilter();
    this.loadCategories();
  }

  private loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (categories: CategoryResponse[]) => {
        const catNames = categories.map(c => c.name);
        this.allCategories.set(['Tất cả', ...catNames]);
      },
      error: (err: any) => {
        console.error('Failed to load categories', err);
      }
    });
  }

  @HostListener('window:resize')
  onResize() {
    this.checkResponsiveFilter();
  }

  private checkResponsiveFilter() {
    if (isPlatformBrowser(this.platformId)) {
      const desktop = window.innerWidth >= 1024;
      this.isDesktop.set(desktop);
      if (desktop) {
        this.filterOpen.set(true);
      }
    }
  }

  sortOptions: SortOption[] = [
    { label: 'Mới nhất', value: 'createdDate,desc', icon: 'schedule' },
    { label: 'Cũ nhất', value: 'createdDate,asc', icon: 'history' },
    { label: 'Xem nhiều nhất', value: 'views,desc', icon: 'trending_up' },
    { label: 'Được thích nhiều nhất', value: 'likes,desc', icon: 'favorite' },
    { label: 'Nhiều bình luận nhất', value: 'comments,desc', icon: 'forum' }
  ];

  selectedSort = signal<SortOption>(this.sortOptions[0]);

  allCategories = signal<string[]>(['Tất cả']);
  selectedCategories = signal<string[]>([]);

  categorySearchQuery = signal('');
  isCategoryDropdownOpen = signal(false);

  filteredCategories = computed(() => {
    const query = this.categorySearchQuery().toLowerCase().trim();
    const categories = this.allCategories();
    if (!query) {
      return categories;
    }
    return categories.filter(cat => cat.toLowerCase().includes(query));
  });

  onCategorySearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.categorySearchQuery.set(target.value);
    this.isCategoryDropdownOpen.set(true);
  }

  toggleCategoryDropdown() {
    this.isCategoryDropdownOpen.update(v => !v);
  }

  isCategorySelected(cat: string): boolean {
    const current = this.selectedCategories();
    if (cat === 'Tất cả') {
      return current.length === 0;
    }
    return current.includes(cat);
  }

  toggleCategorySelection(cat: string) {
    if (cat === 'Tất cả') {
      this.selectedCategories.set([]);
      return;
    }
    this.selectedCategories.update(current => {
      if (current.includes(cat)) {
        return current.filter(c => c !== cat);
      } else {
        return [...current, cat];
      }
    });
  }

  removeCategory(cat: string, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    this.selectedCategories.update(current => current.filter(c => c !== cat));
  }

  clearCategories() {
    this.selectedCategories.set([]);
  }

  selectCategoryFromSearch(cat: string) {
    if (cat === 'Tất cả') {
      this.selectedCategories.set([]);
    } else {
      this.selectedCategories.update(current => {
        if (!current.includes(cat)) {
          return [...current, cat];
        }
        return current;
      });
    }
    this.categorySearchQuery.set('');
    this.isCategoryDropdownOpen.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.category-search-container')) {
      this.isCategoryDropdownOpen.set(false);
    }
  }


  toggleFilter() {
    this.filterOpen.update(v => !v);
  }

  toggleSort() {
    this.sortOpen.update(v => !v);
  }

  selectSort(option: SortOption) {
    this.selectedSort.set(option);
    this.sortOpen.set(false);
  }

  // Filter state & methods aligned with git commit UI
  selectedTimeFilter = signal('Tháng này');
  timeFilters: string[] = ['Hôm nay', 'Tuần này', 'Tháng này', 'Tùy chọn'];

  interactionFilterOptions: InteractionFilterOption[] = [
    { key: 'saved', label: 'Được lưu nhiều nhất' },
    { key: 'liked', label: 'Được thích nhiều nhất' },
    { key: 'viewed', label: 'Xem nhiều nhất' },
    { key: 'discussed', label: 'Có thảo luận sôi nổi' }
  ];

  interactionFilters = signal<Record<string, boolean>>({
    'saved': false,
    'liked': false,
    'viewed': false,
    'discussed': false
  });

  selectTimeFilter(time: string) {
    this.selectedTimeFilter.set(time);
  }

  toggleInteractionFilter(key: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    this.interactionFilters.update(current => ({ ...current, [key]: checked }));
  }

  resetFilters() {
    this.clearCategories();
    this.selectedTimeFilter.set('Tháng này');
    this.interactionFilters.set({
      'saved': false,
      'liked': false,
      'viewed': false,
      'discussed': false
    });
  }

  applyFilters() {
    this.filterOpen.set(false);
  }

  articles: BlogResponse[] = [
    {
      id: 1,
      title: 'Khám phá các tính năng mới trong TypeScript 5.4 và cách áp dụng vào dự án lớn',
      description: 'Tìm hiểu về NoInfer utility type, cải tiến trong việc xác định kiểu dữ liệu trong closure và những thay đổi quan trọng giúp code an toàn hơn.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 1, name: 'TypeScript', slug: 'typescript' },
      createdDate: new Date('2024-05-24').toISOString(),
      modifiedDate: new Date('2024-05-24').toISOString(),
      author: { id: 1, email: 'linh@example.com', avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=100' },
      likesCount: 142,
      commentsCount: 28,
      viewsCount: 1520,
      sharesCount: 12
    },
    {
      id: 2,
      title: 'Tối ưu hóa hiệu năng ứng dụng với Angular 18 Zoneless và Signals',
      description: 'Hướng dẫn chi tiết từng bước chuyển đổi ứng dụng Angular truyền thống sang mô hình Zoneless, kết hợp với Signals để đạt hiệu năng tối đa.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 2, name: 'Angular', slug: 'angular' },
      createdDate: new Date('2024-05-22').toISOString(),
      modifiedDate: new Date('2024-05-22').toISOString(),
      author: { id: 2, email: 'hoang@example.com', avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=100' },
      likesCount: 385,
      commentsCount: 64,
      viewsCount: 4100,
      sharesCount: 34
    },
    {
      id: 3,
      title: 'Xây dựng RESTful API bảo mật cao với Spring Boot 3 và Spring Security 6',
      description: 'Thiết lập xác thực JWT, Role-based Access Control (RBAC) và bảo vệ ứng dụng khỏi các lỗ hổng bảo mật phổ biến theo chuẩn OWASP Top 10.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 3, name: 'Spring Boot', slug: 'spring-boot' },
      createdDate: new Date('2024-05-18').toISOString(),
      modifiedDate: new Date('2024-05-18').toISOString(),
      author: { id: 3, email: 'tuan@example.com', avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=100' },
      likesCount: 210,
      commentsCount: 45,
      viewsCount: 2010,
      sharesCount: 18
    },
    {
      id: 4,
      title: 'Làm chủ CSS Grid và Flexbox để thiết kế giao diện Responsive hiện đại',
      description: 'Khi nào nên dùng Grid và khi nào dùng Flexbox? Các kỹ năng bố trí layout phức tạp mà không cần phụ thuộc quá nhiều vào CSS frameworks.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 4, name: 'CSS', slug: 'css' },
      createdDate: new Date('2024-05-15').toISOString(),
      modifiedDate: new Date('2024-05-15').toISOString(),
      author: { id: 4, email: 'mai@example.com', avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=100' },
      likesCount: 198,
      commentsCount: 32,
      viewsCount: 1800,
      sharesCount: 22
    },
    {
      id: 5,
      title: 'So sánh chi tiết PostgreSQL và MySQL trong các hệ thống phân tán quy mô lớn',
      description: 'Phân tích hiệu suất đọc/ghi, hỗ trợ JSON, khả năng mở rộng và ACID compliance để giúp bạn đưa ra lựa chọn đúng đắn cho hệ thống.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 5, name: 'Database', slug: 'database' },
      createdDate: new Date('2024-05-10').toISOString(),
      modifiedDate: new Date('2024-05-10').toISOString(),
      author: { id: 5, email: 'duc@example.com', avatarUrl: 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&q=80&w=100' },
      likesCount: 276,
      commentsCount: 51,
      viewsCount: 3100,
      sharesCount: 40
    },
    {
      id: 6,
      title: 'Tự động hóa triển khai ứng dụng với Docker, Kubernetes và GitHub Actions CI/CD',
      description: 'Xây dựng luồng CI/CD hoàn chỉnh từ viết test, build container image đến tự động deploy lên cụm Kubernetes trên cloud.',
      content: '',
      status: BlogStatus.PUBLISHED,
      category: { id: 6, name: 'DevOps', slug: 'devops' },
      createdDate: new Date('2024-05-05').toISOString(),
      modifiedDate: new Date('2024-05-05').toISOString(),
      author: { id: 6, email: 'hai@example.com', avatarUrl: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=100' },
      likesCount: 412,
      commentsCount: 89,
      viewsCount: 5200,
      sharesCount: 65
    }
  ];
}
