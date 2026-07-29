import { Component, HostListener, ElementRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationDropdownComponent } from './notification-dropdown/notification-dropdown.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, NotificationDropdownComponent],
  templateUrl: './header.component.html',
})
export class HeaderComponent implements OnInit {
  private readonly elementRef = inject(ElementRef);
  private readonly notificationService = inject(NotificationService);
  readonly authService = inject(AuthService);

  readonly mobileMenuOpen = signal(false);
  readonly notificationDropdownOpen = signal(false);
  readonly userDropdownOpen = signal(false);

  readonly unreadCount$ = this.notificationService.unreadCount$;

  navLinks = [
    { label: 'Trang chủ', path: '/' },
    { label: 'Khám phá', path: '/search' },
    { label: 'Viết bài', path: '/blog/creation' },
  ];

  ngOnInit(): void {
    if (this.authService.currentUser()) {
      // Initial fetch of notifications & unread count
      this.notificationService.getUnreadCount().subscribe();
      this.notificationService.getNotifications().subscribe();
    }
  }

  toggleMobileMenu(): void {
    this.notificationDropdownOpen.set(false);
    this.userDropdownOpen.set(false);
    this.mobileMenuOpen.update((open) => !open);
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  toggleNotificationDropdown(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.mobileMenuOpen.set(false);
    this.userDropdownOpen.set(false);
    this.notificationDropdownOpen.update((open) => {
      const willOpen = !open;
      if (willOpen) {
        this.notificationService.getNotifications().subscribe();
      }
      return willOpen;
    });
  }

  closeNotificationDropdown(): void {
    this.notificationDropdownOpen.set(false);
  }

  toggleUserDropdown(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.mobileMenuOpen.set(false);
    this.notificationDropdownOpen.set(false);
    this.userDropdownOpen.update((open) => !open);
  }

  closeUserDropdown(): void {
    this.userDropdownOpen.set(false);
  }

  logout(): void {
    this.closeUserDropdown();
    this.closeMobileMenu();
    this.authService.logout().subscribe();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.closeNotificationDropdown();
      this.closeUserDropdown();
    }
  }

  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth >= 768) {
      this.closeMobileMenu();
    }
  }
}
