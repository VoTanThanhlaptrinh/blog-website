import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  { path: 'change-password', loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent) },
  { path: 'forgot-password', loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'reset-password', loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
  { path: 'active-account', loadComponent: () => import('./active-account/active-account.component').then(m => m.ActiveAccountComponent) },
  { path: '', redirectTo: 'forgot-password', pathMatch: 'full' }
];

