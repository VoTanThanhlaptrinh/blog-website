import { Routes } from '@angular/router';
import { NotificationSettingsComponent } from './notification-settings/notification-settings.component';

export const SETTINGS_ROUTES: Routes = [
  { path: 'notifications', component: NotificationSettingsComponent },
  { path: '', redirectTo: 'notifications', pathMatch: 'full' }
];
