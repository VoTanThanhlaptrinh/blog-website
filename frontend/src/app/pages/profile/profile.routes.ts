import { Routes } from '@angular/router';
import { ProfileFiltersComponent } from './profile-filters/profile-filters.component';
import { ProfileSplitComponent } from './profile-split/profile-split.component';
import { UpdateProfileComponent } from './update-profile/update-profile.component';

export const PROFILE_ROUTES: Routes = [
  { path: 'filters', component: ProfileFiltersComponent },
  { path: 'split', component: ProfileSplitComponent },
  { path: 'update', component: UpdateProfileComponent },
  { path: '', redirectTo: 'split', pathMatch: 'full' }
];
