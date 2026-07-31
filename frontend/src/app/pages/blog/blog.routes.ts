import { Routes } from '@angular/router';
import { BlogCreationSplitComponent } from './blog-creation-split/blog-creation-split.component';
import { BlogDetailAuthorComponent } from './blog-detail-author/blog-detail-author.component';
import { authGuard } from '../../core/guards/auth.guard';

export const BLOG_ROUTES: Routes = [
  { path: 'creation', component: BlogCreationSplitComponent, canActivate: [authGuard] },
  { path: 'edit/:id', component: BlogCreationSplitComponent, canActivate: [authGuard] },
  { path: 'detail/:id', component: BlogDetailAuthorComponent },
  { path: '', redirectTo: 'detail', pathMatch: 'full' }
];
