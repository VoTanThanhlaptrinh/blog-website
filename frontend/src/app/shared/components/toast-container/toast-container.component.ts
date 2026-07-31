import { Component, inject, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';
import { ToastItem } from '../../../core/models/toast.model';

@Component({
  selector: 'app-single-toast-item',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="relative overflow-hidden w-full max-w-sm rounded-xl p-4 shadow-xl border bg-white flex items-start gap-3 transition-all duration-300 pointer-events-auto group animate-slide-in-right"
      [ngClass]="{
        'border-emerald-200 shadow-emerald-500/10': toast.type === 'success',
        'border-rose-200 shadow-rose-500/10': toast.type === 'error',
        'border-amber-200 shadow-amber-500/10': toast.type === 'warning',
        'border-sky-200 shadow-sky-500/10': toast.type === 'info'
      }"
      (mouseenter)="pause()"
      (mouseleave)="resume()">

      <!-- Icon -->
      <div [ngClass]="{
        'bg-emerald-100 text-emerald-600': toast.type === 'success',
        'bg-rose-100 text-rose-600': toast.type === 'error',
        'bg-amber-100 text-amber-600': toast.type === 'warning',
        'bg-sky-100 text-sky-600': toast.type === 'info'
      }" class="flex-shrink-0 w-9 h-9 rounded-full flex items-center justify-center">
        <span class="material-symbols-outlined text-[20px]">
          @if (toast.type === 'success') { check_circle }
          @else if (toast.type === 'error') { error }
          @else if (toast.type === 'warning') { warning }
          @else { info }
        </span>
      </div>

      <!-- Message & Title -->
      <div class="flex-1 space-y-0.5 pt-0.5">
        @if (toast.title) {
          <h4 class="text-sm font-bold text-stone-900 leading-snug">{{ toast.title }}</h4>
        }
        <p class="text-xs font-medium text-stone-600 leading-relaxed">{{ toast.message }}</p>
      </div>

      <!-- Close Button -->
      <button 
        type="button"
        (click)="close()"
        class="flex-shrink-0 text-stone-400 hover:text-stone-700 p-1 rounded-lg transition-colors cursor-pointer">
        <span class="material-symbols-outlined text-[18px]">close</span>
      </button>

      <!-- Progress Bar -->
      <div class="absolute bottom-0 left-0 right-0 h-1 bg-stone-100 overflow-hidden">
        <div 
          class="h-full transition-all duration-75 ease-linear"
          [ngClass]="{
            'bg-emerald-500': toast.type === 'success',
            'bg-rose-500': toast.type === 'error',
            'bg-amber-500': toast.type === 'warning',
            'bg-sky-500': toast.type === 'info'
          }"
          [style.width.%]="progressPercent">
        </div>
      </div>
    </div>
  `
})
export class SingleToastItemComponent implements OnInit, OnDestroy {
  @Input({ required: true }) toast!: ToastItem;
  private toastService = inject(ToastService);

  progressPercent = 100;
  private intervalId: any;
  private isPaused = false;
  private totalDuration = 4000;
  private remainingTime = 4000;

  ngOnInit(): void {
    this.totalDuration = this.toast.duration || 4000;
    this.remainingTime = this.totalDuration;
    this.startTimer();
  }

  startTimer(): void {
    const stepMs = 40;
    this.intervalId = setInterval(() => {
      if (!this.isPaused) {
        this.remainingTime -= stepMs;
        this.progressPercent = Math.max(0, (this.remainingTime / this.totalDuration) * 100);

        if (this.remainingTime <= 0) {
          this.close();
        }
      }
    }, stepMs);
  }

  pause(): void {
    this.isPaused = true;
  }

  resume(): void {
    this.isPaused = false;
  }

  close(): void {
    this.clearInterval();
    this.toastService.remove(this.toast.id);
  }

  private clearInterval(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  ngOnDestroy(): void {
    this.clearInterval();
  }
}

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, SingleToastItemComponent],
  templateUrl: './toast-container.component.html'
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);
}
