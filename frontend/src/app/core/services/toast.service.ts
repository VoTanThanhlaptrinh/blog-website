import { Injectable, signal } from '@angular/core';
import { ToastItem, ToastType } from '../models/toast.model';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  readonly toasts = signal<ToastItem[]>([]);

  show(message: string, title?: string, type: ToastType = 'info', duration: number = 4000): void {
    const id = 'toast_' + Math.random().toString(36).substring(2, 9) + '_' + Date.now();
    const newToast: ToastItem = {
      id,
      type,
      message,
      title,
      duration
    };

    this.toasts.update(current => [...current, newToast]);
  }

  success(message: string, title?: string, duration?: number): void {
    this.show(message, title, 'success', duration);
  }

  error(message: string, title?: string, duration?: number): void {
    this.show(message, title, 'error', duration);
  }

  info(message: string, title?: string, duration?: number): void {
    this.show(message, title, 'info', duration);
  }

  warning(message: string, title?: string, duration?: number): void {
    this.show(message, title, 'warning', duration);
  }

  remove(id: string): void {
    this.toasts.update(current => current.filter(t => t.id !== id));
  }
}
