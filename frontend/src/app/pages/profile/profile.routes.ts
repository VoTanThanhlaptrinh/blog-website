import { Routes } from '@angular/router';
import { ProfileLayoutComponent } from './profile-layout/profile-layout.component';
import { ProfilePostsComponent } from './profile-posts/profile-posts.component';
import { ProfileSavedComponent } from './profile-saved/profile-saved.component';
import { ProfileDraftsComponent } from './profile-drafts/profile-drafts.component';
import { UpdateProfileComponent } from './update-profile/update-profile.component';

export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    component: ProfileLayoutComponent,
    children: [
      { path: '', redirectTo: 'posts', pathMatch: 'full' },
      { path: 'posts', component: ProfilePostsComponent },
      { path: 'saved', component: ProfileSavedComponent },
      { path: 'drafts', component: ProfileDraftsComponent },
    ]
  },
  { path: 'update', component: UpdateProfileComponent }
];
