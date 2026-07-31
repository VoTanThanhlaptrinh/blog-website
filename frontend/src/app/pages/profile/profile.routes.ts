import { Routes } from '@angular/router';

export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./profile-layout/profile-layout.component').then(m => m.ProfileLayoutComponent),
    children: [
      { path: '', redirectTo: 'posts', pathMatch: 'full' },
      { 
        path: 'posts', 
        loadComponent: () => import('./profile-posts/profile-posts.component').then(m => m.ProfilePostsComponent) 
      },
      { 
        path: 'saved', 
        loadComponent: () => import('./profile-saved/profile-saved.component').then(m => m.ProfileSavedComponent) 
      },
      { 
        path: 'drafts', 
        redirectTo: 'posts',
        pathMatch: 'full'
      },
    ]
  },
  { 
    path: 'update', 
    loadComponent: () => import('./update-profile/update-profile.component').then(m => m.UpdateProfileComponent) 
  }
];
