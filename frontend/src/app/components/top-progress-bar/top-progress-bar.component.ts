import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, Event as RouterEvent, NavigationStart, NavigationEnd, NavigationCancel, NavigationError } from '@angular/router';

@Component({
  selector: 'app-top-progress-bar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="fixed top-0 left-0 h-[3px] bg-accent-rust z-[9999] pointer-events-none transition-all duration-300 ease-out"
      [style.width.%]="progress()"
      [style.opacity]="visible() ? '1' : '0'"
    ></div>
  `
})
export class TopProgressBarComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  
  progress = signal<number>(0);
  visible = signal<boolean>(false);
  
  private intervalId: any;
  private timeoutId: any;

  ngOnInit() {
    this.router.events.subscribe((event: RouterEvent) => {
      if (event instanceof NavigationStart) {
        this.startProgress();
      } else if (
        event instanceof NavigationEnd ||
        event instanceof NavigationCancel ||
        event instanceof NavigationError
      ) {
        this.completeProgress();
      }
    });
  }

  private startProgress() {
    this.clearTimers();
    this.progress.set(0);
    
    // Slight delay before showing to prevent flashing on very fast navigations
    this.timeoutId = setTimeout(() => {
      this.visible.set(true);
      this.progress.set(15);

      this.intervalId = setInterval(() => {
        this.progress.update(val => {
          if (val >= 85) {
            clearInterval(this.intervalId);
            return val;
          }
          return val + (Math.random() * 5 + 1);
        });
      }, 300);
    }, 50);
  }

  private completeProgress() {
    this.clearTimers();
    this.visible.set(true);
    this.progress.set(100);

    // Wait for the width transition to finish before hiding
    this.timeoutId = setTimeout(() => {
      this.visible.set(false);
      
      // Reset width to 0 after opacity transition finishes
      setTimeout(() => {
        if (!this.visible()) {
          this.progress.set(0);
        }
      }, 300);
    }, 400);
  }

  private clearTimers() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }

  ngOnDestroy() {
    this.clearTimers();
  }
}
