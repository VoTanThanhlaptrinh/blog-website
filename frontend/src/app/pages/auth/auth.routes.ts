import { Routes } from '@angular/router';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { ActiveAccountComponent } from './active-account/active-account.component';

export const AUTH_ROUTES: Routes = [
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'active-account', component: ActiveAccountComponent },
  { path: '', redirectTo: 'forgot-password', pathMatch: 'full' }
];

