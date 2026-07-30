import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationResponse, NotificationType } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-dropdown',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-dropdown.component.html',
})
export class NotificationDropdownComponent {
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  @Output() closeDropdown = new EventEmitter<void>();

  readonly unreadCount$ = this.notificationService.unreadCount$;
  readonly activeTab = signal<'all' | 'unread'>('all');
  readonly allNotifications$ = this.notificationService.notifications$;

  getFilteredList(items: NotificationResponse[] | null): NotificationResponse[] {
    if (!items) return [];
    if (this.activeTab() === 'unread') {
      return items.filter((n) => !n.isRead);
    }
    return items;
  }

  setActiveTab(tab: 'all' | 'unread'): void {
    this.activeTab.set(tab);
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe();
  }

  onNotificationClick(item: NotificationResponse): void {
    if (!item.isRead) {
      this.notificationService.markAsRead(item.id).subscribe();
    }
    this.closeDropdown.emit();
    if (item.relatedUrl) {
      this.router.navigateByUrl(item.relatedUrl);
    }
  }

  getIcon(type: NotificationType): string {
    switch (type) {
      case NotificationType.COMMENT:
        return '💬';
      case NotificationType.LIKE:
        return '❤️';
      case NotificationType.BLOG_APPROVED:
        return '✅';
      case NotificationType.BLOG_REJECTED:
        return '❌';
      case NotificationType.SYSTEM:
      default:
        return '🔔';
    }
  }

  getRelativeTime(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    return date.toLocaleDateString('vi-VN');
  }
}
