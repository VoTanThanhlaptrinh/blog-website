import { Routes } from '@angular/router';
import { BlogCreationSplitComponent } from './blog-creation-split/blog-creation-split.component';
import { BlogDetailAuthorComponent } from './blog-detail-author/blog-detail-author.component';

export const BLOG_ROUTES: Routes = [
  { path: 'creation', component: BlogCreationSplitComponent },
  { path: 'detail/:id', component: BlogDetailAuthorComponent },
  { path: '', redirectTo: 'detail', pathMatch: 'full' }
];
