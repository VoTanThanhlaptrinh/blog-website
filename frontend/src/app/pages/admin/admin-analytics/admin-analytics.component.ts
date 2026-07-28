import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminDashboardService } from '../../../core/services/admin-dashboard.service';

@Component({
  selector: 'app-admin-analytics',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-analytics.component.html',
  styleUrl: './admin-analytics.component.scss'
})
export class AdminAnalyticsComponent implements OnInit {
  protected readonly dashboardService = inject(AdminDashboardService);

  readonly summary$ = this.dashboardService.summary$;
  readonly growthStats$ = this.dashboardService.growthStats$;
  readonly topBlogs$ = this.dashboardService.topBlogs$;
  readonly loading$ = this.dashboardService.loading$;
  readonly error$ = this.dashboardService.error$;

  selectedDays = 30;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.dashboardService.getDashboardSummary().subscribe();
    this.dashboardService.getGrowthStats(this.selectedDays).subscribe();
    this.dashboardService.getTopBlogs(10).subscribe();
  }

  onDaysChange(days: number): void {
    this.selectedDays = days;
    this.dashboardService.getGrowthStats(days).subscribe();
  }
}
