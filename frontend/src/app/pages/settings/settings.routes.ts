import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./settings-layout.component').then((m) => m.SettingsLayoutComponent),
    children: [
      {
        path: '',
        redirectTo: 'profile',
        pathMatch: 'full'
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./profile-settings/profile-settings.component').then(
            (m) => m.ProfileSettingsComponent
          )
      },
      {
        path: 'account',
        loadComponent: () =>
          import('./account-settings/account-settings.component').then(
            (m) => m.AccountSettingsComponent
          )
      },
      {
        path: 'security',
        loadComponent: () =>
          import('./security-settings/security-settings.component').then(
            (m) => m.SecuritySettingsComponent
          )
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./notification-settings/notification-settings.component').then(
            (m) => m.NotificationSettingsComponent
          )
      },
      {
        path: 'appearance',
        loadComponent: () =>
          import('./appearance-settings/appearance-settings.component').then(
            (m) => m.AppearanceSettingsComponent
          )
      }
    ]
  }
];
