import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-appearance-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appearance-settings.component.html'
})
export class AppearanceSettingsComponent implements OnInit {
  selectedTheme = signal<'light' | 'dark' | 'system'>('system');

  ngOnInit(): void {
    const saved = (localStorage.getItem('app-theme') as 'light' | 'dark' | 'system') || 'system';
    this.selectedTheme.set(saved);
  }

  setTheme(theme: 'light' | 'dark' | 'system'): void {
    this.selectedTheme.set(theme);
    localStorage.setItem('app-theme', theme);

    if (theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}
