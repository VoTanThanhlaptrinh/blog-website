export enum NotificationType {
  LIKE = 'LIKE',
  COMMENT = 'COMMENT',
  SYSTEM = 'SYSTEM',
  BLOG_APPROVED = 'BLOG_APPROVED',
  BLOG_REJECTED = 'BLOG_REJECTED'
}

export interface NotificationResponse {
  id: number;
  title: string;
  content: string;
  type: NotificationType;
  isRead: boolean;
  relatedUrl?: string;
  createdDate: string;
}
