import { Routes } from '@angular/router';
import { AdminAnalyticsComponent } from './admin-analytics/admin-analytics.component';
import { AdminContentManagementComponent } from './admin-content-management/admin-content-management.component';
import { AdminReportsComponent } from './admin-reports/admin-reports.component';
import { AdminSystemSettingsComponent } from './admin-system-settings/admin-system-settings.component';
import { AdminUserManagementComponent } from './admin-user-management/admin-user-management.component';

export const ADMIN_ROUTES: Routes = [
  { path: 'analytics', component: AdminAnalyticsComponent },
  { path: 'content-management', component: AdminContentManagementComponent },
  { path: 'reports', component: AdminReportsComponent },
  { path: 'system-settings', component: AdminSystemSettingsComponent },
  { path: 'user-management', component: AdminUserManagementComponent },
  { path: '', redirectTo: 'analytics', pathMatch: 'full' }
];
