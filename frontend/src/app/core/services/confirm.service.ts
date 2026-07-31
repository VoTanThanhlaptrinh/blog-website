import { Injectable, signal } from '@angular/core';
import { ConfirmOptions, ConfirmState } from '../models/confirm.model';

@Injectable({
  providedIn: 'root'
})
export class ConfirmService {
  private resolveFn: ((value: boolean) => void) | null = null;

  readonly modalState = signal<ConfirmState>({
    isOpen: false,
    options: {
      title: '',
      message: ''
    }
  });

  confirm(options: ConfirmOptions): Promise<boolean> {
    // If there's an ongoing unresolved confirm, resolve it as false before starting a new one
    if (this.resolveFn) {
      this.resolveFn(false);
    }

    this.modalState.set({
      isOpen: true,
      options: {
        confirmText: 'Đồng ý',
        cancelText: 'Hủy',
        actionType: 'primary',
        ...options
      }
    });

    return new Promise<boolean>((resolve) => {
      this.resolveFn = resolve;
    });
  }

  respond(result: boolean): void {
    if (this.resolveFn) {
      this.resolveFn(result);
      this.resolveFn = null;
    }

    this.modalState.update(state => ({
      ...state,
      isOpen: false
    }));
  }
}
