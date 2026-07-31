import { Component, signal, computed, HostListener, OnInit, Inject, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CategoryService } from '../../../core/services/category.service';
import { BlogService } from '../../../core/services/blog.service';
import { CategoryResponse, BlogResponse, BlogStatus } from '../../../core/models/blog.model';
import { BlogCardComponent } from '../../../shared/components/blog-card/blog-card.component';
import { finalize } from 'rxjs';

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
  private readonly blogService = inject(BlogService);

  // Pagination & Data Signals
  blogs = signal<BlogResponse[]>([]);
  loading = signal(false);
  hasMore = signal(false);
  nextCursor = signal<number | undefined>(undefined);

  keywordSearchQuery = signal('');

  ngOnInit() {
    this.checkResponsiveFilter();
    this.loadCategories();
    this.search();
  }

  private loadCategories() {
    this.categoryService.getCategories().subscribe((categories: CategoryResponse[]) => {
      const catNames = categories.map(c => c.name);
      this.allCategories.set(['Tất cả', ...catNames]);
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
    this.search();
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
    this.search();
  }

  onKeywordSearchInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.keywordSearchQuery.set(target.value);
  }

  onKeywordSearchEnter() {
    this.search();
  }

  search(isLoadMore = false) {
    if (this.loading()) return;
    
    this.loading.set(true);
    const keyword = this.keywordSearchQuery().trim();
    const categories = this.selectedCategories().length > 0 ? this.selectedCategories() : undefined;
    const lastId = isLoadMore ? this.nextCursor() : undefined;

    const query = {
      keyword: keyword || undefined,
      categories: categories,
      lastId: lastId,
      limit: 6
    };

    this.blogService
      .searchBlogsCursor(query)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe((res) => {
        if (isLoadMore) {
          this.blogs.update(current => [...current, ...res.content]);
        } else {
          this.blogs.set(res.content);
        }
        this.hasMore.set(res.hasMore);
        this.nextCursor.set(res.nextCursor);
      });
  }

  loadMore() {
    if (this.hasMore()) {
      this.search(true);
    }
  }

}
