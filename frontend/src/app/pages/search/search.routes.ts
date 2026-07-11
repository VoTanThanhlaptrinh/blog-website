import { Routes } from '@angular/router';
import { SearchCategoryGridComponent } from './search-category-grid/search-category-grid.component';
import { SearchCategoryRowComponent } from './search-category-row/search-category-row.component';

export const SEARCH_ROUTES: Routes = [
  { path: 'grid', component: SearchCategoryGridComponent },
  { path: 'row', component: SearchCategoryRowComponent },
  { path: '', redirectTo: 'grid', pathMatch: 'full' }
];
