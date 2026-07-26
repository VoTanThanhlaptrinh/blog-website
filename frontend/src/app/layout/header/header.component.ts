import { Component, HostListener, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  mobileMenuOpen = signal(false);

  navLinks = [
    { label: 'Trang chủ', path: '/' },
    { label: 'Khám phá', path: '/search' },
    { label: 'Viết bài', path: '/blog/creation' },
  ];

  toggleMobileMenu() {
    this.mobileMenuOpen.update((open) => !open);
  }

  closeMobileMenu() {
    this.mobileMenuOpen.set(false);
  }

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth >= 768) {
      this.closeMobileMenu();
    }
  }
}
