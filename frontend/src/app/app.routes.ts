import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/auth/register/register.component').then(
        (m) => m.RegisterComponent,
      ),
  },
  {
    path: 'oauth2/redirect',
    loadComponent: () =>
      import('./pages/auth/oauth2-redirect/oauth2-redirect.component').then(
        (m) => m.Oauth2RedirectComponent,
      ),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./pages/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadChildren: () => import('./pages/profile/profile.routes').then(m => m.PROFILE_ROUTES)
  },
  {
    path: 'auth',
    loadChildren: () => import('./pages/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'blog',
    loadChildren: () => import('./pages/blog/blog.routes').then(m => m.BLOG_ROUTES)
  },
  {
    path: 'search',
    loadChildren: () => import('./pages/search/search.routes').then(m => m.SEARCH_ROUTES)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadChildren: () => import('./pages/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
  }
];
