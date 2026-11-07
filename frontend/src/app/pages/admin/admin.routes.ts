import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('../../layout/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { 
        path: 'analytics', 
        loadComponent: () => import('./admin-analytics/admin-analytics.component').then(m => m.AdminAnalyticsComponent) 
      },
      { 
        path: 'content-management', 
        loadComponent: () => import('./admin-content-management/admin-content-management.component').then(m => m.AdminContentManagementComponent) 
      },
      { 
        path: 'reports', 
        loadComponent: () => import('./admin-reports/admin-reports.component').then(m => m.AdminReportsComponent) 
      },
      { 
        path: 'system-settings', 
        loadComponent: () => import('./admin-system-settings/admin-system-settings.component').then(m => m.AdminSystemSettingsComponent) 
      },
      { 
        path: 'user-management', 
        loadComponent: () => import('./admin-user-management/admin-user-management.component').then(m => m.AdminUserManagementComponent) 
      },
      {
        path: 'articles/review/:id',
        loadComponent: () => import('./admin-article-review/admin-article-review.component').then(m => m.AdminArticleReviewComponent)
      },
      { path: '', redirectTo: 'analytics', pathMatch: 'full' }
    ]
  }
];
