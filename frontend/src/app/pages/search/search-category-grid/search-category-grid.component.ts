import { Component, signal, computed, HostListener, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export interface SortOption {
  label: string;
  value: string;
  icon: string;
}

export interface InteractionFilterOption {
  key: string;
  label: string;
}

export interface ArticleCard {
  id: number;
  title: string;
  excerpt: string;
  category: string;
  date: string;
  readTime: string;
  image: string;
  author: {
    name: string;
    avatar: string;
  };
  likes: number;
  comments: number;
}

@Component({
  selector: 'app-search-category-grid',
  imports: [],
  templateUrl: './search-category-grid.component.html',
  styleUrl: './search-category-grid.component.scss'
})
export class SearchCategoryGridComponent implements OnInit {
  filterOpen = signal(false);
  sortOpen = signal(false);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngOnInit() {
    this.checkResponsiveFilter();
  }

  @HostListener('window:resize')
  onResize() {
    this.checkResponsiveFilter();
  }

  private checkResponsiveFilter() {
    if (isPlatformBrowser(this.platformId)) {
      this.filterOpen.set(window.innerWidth >= 768);
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

  allCategories: string[] = [
    'Tất cả', 'Angular', 'Spring Boot', 'CSS', 'Database', 'Security', 'DevOps', 'TypeScript',
    'React', 'Vue.js', 'Node.js', 'Python', 'Golang', 'Java', 'Microservices', 'Docker',
    'Kubernetes', 'Cloud Computing', 'GraphQL', 'REST API', 'UI/UX Design', 'Machine Learning', 'AI'
  ];
  categories: string[] = ['Tất cả', 'Angular', 'Spring Boot', 'CSS', 'Database', 'Security', 'DevOps', 'TypeScript'];
  selectedCategory = signal('Tất cả');

  categorySearchQuery = signal('');
  isCategoryDropdownOpen = signal(false);

  filteredCategories = computed(() => {
    const query = this.categorySearchQuery().toLowerCase().trim();
    if (!query) {
      return this.allCategories;
    }
    return this.allCategories.filter(cat => cat.toLowerCase().includes(query));
  });

  onCategorySearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.categorySearchQuery.set(target.value);
    this.isCategoryDropdownOpen.set(true);
  }

  toggleCategoryDropdown() {
    this.isCategoryDropdownOpen.update(v => !v);
  }

  selectCategoryFromSearch(cat: string) {
    this.selectedCategory.set(cat);
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

  isDragging = false;
  didDrag = false;
  startX = 0;
  scrollLeft = 0;

  onMouseDown(event: MouseEvent, container: HTMLElement) {
    this.isDragging = true;
    this.didDrag = false;
    container.classList.add('cursor-grabbing');
    container.classList.remove('scroll-smooth');
    this.startX = event.pageX - container.offsetLeft;
    this.scrollLeft = container.scrollLeft;
  }

  onMouseLeave(container: HTMLElement) {
    if (!this.isDragging) return;
    this.isDragging = false;
    container.classList.remove('cursor-grabbing');
    container.classList.add('scroll-smooth');
  }

  onMouseUp(container: HTMLElement) {
    if (!this.isDragging) return;
    this.isDragging = false;
    container.classList.remove('cursor-grabbing');
    container.classList.add('scroll-smooth');
  }

  onMouseMove(event: MouseEvent, container: HTMLElement) {
    if (!this.isDragging) return;
    event.preventDefault();
    const x = event.pageX - container.offsetLeft;
    const walk = (x - this.startX) * 1.5;
    if (Math.abs(walk) > 5) {
      this.didDrag = true;
    }
    container.scrollLeft = this.scrollLeft - walk;
  }

  selectCategory(cat: string) {
    if (this.didDrag) {
      this.didDrag = false;
      return;
    }
    this.selectedCategory.set(cat);
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

  articles: ArticleCard[] = [
    {
      id: 1,
      title: 'Khám phá các tính năng mới trong TypeScript 5.4 và cách áp dụng vào dự án lớn',
      excerpt: 'Tìm hiểu về NoInfer utility type, cải tiến trong việc xác định kiểu dữ liệu trong closure và những thay đổi quan trọng giúp code an toàn hơn.',
      category: 'TypeScript',
      date: '24 Tháng 5, 2024',
      readTime: '5 phút đọc',
      image: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Linh Nguyễn',
        avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=100'
      },
      likes: 142,
      comments: 28
    },
    {
      id: 2,
      title: 'Tối ưu hóa hiệu năng ứng dụng với Angular 18 Zoneless và Signals',
      excerpt: 'Hướng dẫn chi tiết từng bước chuyển đổi ứng dụng Angular truyền thống sang mô hình Zoneless, kết hợp với Signals để đạt hiệu năng tối đa.',
      category: 'Angular',
      date: '22 Tháng 5, 2024',
      readTime: '8 phút đọc',
      image: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Hoàng Trần',
        avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=100'
      },
      likes: 385,
      comments: 64
    },
    {
      id: 3,
      title: 'Xây dựng RESTful API bảo mật cao với Spring Boot 3 và Spring Security 6',
      excerpt: 'Thiết lập xác thực JWT, Role-based Access Control (RBAC) và bảo vệ ứng dụng khỏi các lỗ hổng bảo mật phổ biến theo chuẩn OWASP Top 10.',
      category: 'Spring Boot',
      date: '18 Tháng 5, 2024',
      readTime: '12 phút đọc',
      image: 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Tuấn Lê',
        avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=100'
      },
      likes: 210,
      comments: 45
    },
    {
      id: 4,
      title: 'Làm chủ CSS Grid và Flexbox để thiết kế giao diện Responsive hiện đại',
      excerpt: 'Khi nào nên dùng Grid và khi nào dùng Flexbox? Các kỹ năng bố trí layout phức tạp mà không cần phụ thuộc quá nhiều vào CSS frameworks.',
      category: 'CSS',
      date: '15 Tháng 5, 2024',
      readTime: '6 phút đọc',
      image: 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Mai Phạm',
        avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=100'
      },
      likes: 198,
      comments: 32
    },
    {
      id: 5,
      title: 'So sánh chi tiết PostgreSQL và MySQL trong các hệ thống phân tán quy mô lớn',
      excerpt: 'Phân tích hiệu suất đọc/ghi, hỗ trợ JSON, khả năng mở rộng và ACID compliance để giúp bạn đưa ra lựa chọn đúng đắn cho hệ thống.',
      category: 'Database',
      date: '10 Tháng 5, 2024',
      readTime: '10 phút đọc',
      image: 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Đức Vũ',
        avatar: 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&q=80&w=100'
      },
      likes: 276,
      comments: 51
    },
    {
      id: 6,
      title: 'Tự động hóa triển khai ứng dụng với Docker, Kubernetes và GitHub Actions CI/CD',
      excerpt: 'Xây dựng luồng CI/CD hoàn chỉnh từ viết test, build container image đến tự động deploy lên cụm Kubernetes trên cloud.',
      category: 'DevOps',
      date: '05 Tháng 5, 2024',
      readTime: '15 phút đọc',
      image: 'https://images.unsplash.com/photo-1607799279861-4dd421887fb3?auto=format&fit=crop&q=80&w=800',
      author: {
        name: 'Hải Nguyễn',
        avatar: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=100'
      },
      likes: 412,
      comments: 89
    }
  ];
}
