import { Component, signal, computed, HostListener, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export interface SortOption {
  label: string;
  value: string;
  icon: string;
}

@Component({
  selector: 'app-search-category-row',
  imports: [],
  templateUrl: './search-category-row.component.html',
  styleUrl: './search-category-row.component.scss'
})
export class SearchCategoryRowComponent implements OnInit {
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
}
