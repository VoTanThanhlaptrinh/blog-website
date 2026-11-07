# Cẩm Nang Tổng Hợp Cách Viết URL Cho Các Component — Blog-Website

> **Tài liệu kỹ thuật tổng hợp toàn diện** về kiến trúc định tuyến (Routing), quy tắc xây dựng URL, bảng ánh xạ URL giữa Component Frontend và Backend API, cũng như cách điều hướng (Navigation) và trích xuất tham số (Parameters) trong dự án **Blog-Website**.
>
> - **Frontend**: Angular 19+ (Standalone Components, Lazy Loading Routes, Functional Guards, Signals).
> - **Backend**: Spring Boot 3+ (Domain-Driven Design, RESTful APIs, Spring Security & JWT).

---

## 1. Tổng Quan Kiến Trúc Định Tuyến (Routing Architecture)

Hệ thống định tuyến của Frontend được xây dựng hoàn toàn dựa trên cơ chế **Standalone Components** kết hợp **Lazy Loading** (`loadComponent` và `loadChildren`) để tối ưu dung lượng gói tải ban đầu (Initial Bundle Size).

### 1.1. Sơ đồ phân cấp Route chính
```
App Root Router (app.routes.ts)
│
├── / (HomeComponent) [Public]
├── /search (SearchCategoryGridComponent) [Public]
│
├── /login (LoginComponent) [Public, queryParam: returnUrl]
├── /register (RegisterComponent) [Public]
├── /oauth2/redirect (Oauth2RedirectComponent) [Callback]
│
├── /auth (auth.routes.ts)
│   ├── /auth/forgot-password (ForgotPasswordComponent)
│   ├── /auth/reset-password (ResetPasswordComponent, queryParam: token)
│   ├── /auth/active-account (ActiveAccountComponent, queryParam: token)
│   └── /auth/change-password (ChangePasswordComponent)
│
├── /blog (blog.routes.ts)
│   ├── /blog/creation (BlogCreationSplitComponent) [authGuard]
│   ├── /blog/edit/:id (BlogCreationSplitComponent) [authGuard, param: id]
│   └── /blog/detail/:id (BlogDetailAuthorComponent) [Public, param: id]
│
├── /profile (profile.routes.ts) [authGuard]
│   ├── /profile/posts (ProfilePostsComponent)
│   ├── /profile/saved (ProfileSavedComponent)
│   ├── /profile/drafts -> redirect sang /profile/posts
│   └── /profile/update (UpdateProfileComponent)
│
├── /settings (settings.routes.ts) [authGuard]
│   ├── /settings/profile (ProfileSettingsComponent)
│   ├── /settings/account (AccountSettingsComponent)
│   ├── /settings/security (SecuritySettingsComponent)
│   ├── /settings/notifications (NotificationSettingsComponent)
│   └── /settings/appearance (AppearanceSettingsComponent)
│
└── /admin (admin.routes.ts) [adminGuard]
    ├── /admin/analytics (AdminAnalyticsComponent)
    ├── /admin/content-management (AdminContentManagementComponent)
    ├── /admin/reports (AdminReportsComponent)
    ├── /admin/user-management (AdminUserManagementComponent)
    ├── /admin/system-settings (AdminSystemSettingsComponent)
    └── /admin/articles/review/:id (AdminArticleReviewComponent, param: id)
```

### 1.2. Cơ chế Layout và Router Outlets lồng nhau (Nested Router Outlets)
1. **Root Outlet (`app.component.html`)**:
   - Chứa thanh cuộn `<app-top-progress-bar />`, thông báo `<app-toast-container />`, và hộp thoại `<app-confirm-modal />`.
   - Điều khiển hiển thị thanh điều hướng: Khi người dùng truy cập các URL bắt đầu bằng `/admin`, `AppComponent` sẽ tự động ẩn `<app-header>`, `<app-footer>`, và `<app-mobile-navbar>` để nhường toàn bộ không gian cho giao diện Quản trị.
2. **Sub Outlets**:
   - `AdminLayoutComponent`: Cung cấp Sidebar, Topbar và Bottom Navigation di động riêng cho Admin; nội dung từng trang con render tại `<router-outlet>`.
   - `ProfileLayoutComponent`: Cung cấp Card thông tin cá nhân và Menu bên trái (Bài viết / Đã lưu / Cài đặt); nội dung render tại `<router-outlet>`.
   - `SettingsLayoutComponent`: Cung cấp danh mục cấu hình hệ thống (Profile / Account / Security / Notifications / Appearance); nội dung từng tab render tại `<router-outlet>`.

---

## 2. Bảng Tổng Hợp Chi Tiết Toàn Bộ URL & Component (Route Matrix)

Dưới đây là bảng đối chiếu đầy đủ từng Component trong dự án với URL tương ứng, quyền truy cập, các Guard kiểm soát và tham số đi kèm:

| Nhóm Nghiệp Vụ | Tên Component | File Mã Nguồn Component | Đường Dẫn URL Đầy Đủ | Quyền Hạn | Guard Áp Dụng | Path Params | Query Params |
|---|---|---|---|---|---|---|---|
| **Trang chủ** | `HomeComponent` | `pages/home/home.component.ts` | `/` | Public | *Không* | *Không* | *Không* |
| **Tìm kiếm** | `SearchCategoryGridComponent` | `pages/search/search-category-grid/search-category-grid.component.ts` | `/search` | Public | *Không* | *Không* | *Không* |
| **Xác thực** | `LoginComponent` | `pages/auth/login/login.component.ts` | `/login` | Public | *Không* | *Không* | `returnUrl` (URL cần quay lại sau đăng nhập) |
| **Xác thực** | `RegisterComponent` | `pages/auth/register/register.component.ts` | `/register` | Public | *Không* | *Không* | *Không* |
| **Xác thực** | `Oauth2RedirectComponent` | `pages/auth/oauth2-redirect/oauth2-redirect.component.ts` | `/oauth2/redirect` | Public | *Không* | *Không* | Nhận JWT từ Backend OAuth2 |
| **Xác thực** | `ForgotPasswordComponent` | `pages/auth/forgot-password/forgot-password.component.ts` | `/auth/forgot-password` (hoặc `/auth`) | Public | *Không* | *Không* | *Không* |
| **Xác thực** | `ResetPasswordComponent` | `pages/auth/reset-password/reset-password.component.ts` | `/auth/reset-password` | Public | *Không* | *Không* | `token` (Mã xác thực đổi mật khẩu) |
| **Xác thực** | `ActiveAccountComponent` | `pages/auth/active-account/active-account.component.ts` | `/auth/active-account` | Public | *Không* | *Không* | `token` (Mã kích hoạt gửi qua email) |
| **Xác thực** | `ChangePasswordComponent` | `pages/auth/change-password/change-password.component.ts` | `/auth/change-password` | User đã đăng nhập | *Chưa gán guard riêng* | *Không* | *Không* |
| **Bài viết** | `BlogCreationSplitComponent` | `pages/blog/blog-creation-split/blog-creation-split.component.ts` | `/blog/creation` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Bài viết** | `BlogCreationSplitComponent` | `pages/blog/blog-creation-split/blog-creation-split.component.ts` | `/blog/edit/:id` | Tác giả bài viết | `authGuard` | `:id` (ID bài viết) | *Không* |
| **Bài viết** | `BlogDetailAuthorComponent` | `pages/blog/blog-detail-author/blog-detail-author.component.ts` | `/blog/detail/:id` | Public | *Không* | `:id` (ID bài viết) | *Không* |
| **Cá nhân** | `ProfileLayoutComponent` | `pages/profile/profile-layout/profile-layout.component.ts` | `/profile` (Redirect sang `/profile/posts`) | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cá nhân** | `ProfilePostsComponent` | `pages/profile/profile-posts/profile-posts.component.ts` | `/profile/posts` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cá nhân** | `ProfileSavedComponent` | `pages/profile/profile-saved/profile-saved.component.ts` | `/profile/saved` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cá nhân** | `ProfileDraftsComponent` | `pages/profile/profile-drafts/profile-drafts.component.ts` | `/profile/drafts` *(Đang redirect về posts)* | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cá nhân** | `UpdateProfileComponent` | `pages/profile/update-profile/update-profile.component.ts` | `/profile/update` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `SettingsLayoutComponent` | `pages/settings/settings-layout.component.ts` | `/settings` (Redirect sang `/settings/profile`) | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `ProfileSettingsComponent` | `pages/settings/profile-settings/profile-settings.component.ts` | `/settings/profile` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `AccountSettingsComponent` | `pages/settings/account-settings/account-settings.component.ts` | `/settings/account` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `SecuritySettingsComponent` | `pages/settings/security-settings/security-settings.component.ts` | `/settings/security` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `NotificationSettingsComponent` | `pages/settings/notification-settings/notification-settings.component.ts` | `/settings/notifications` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Cài đặt** | `AppearanceSettingsComponent` | `pages/settings/appearance-settings/appearance-settings.component.ts` | `/settings/appearance` | User đã đăng nhập | `authGuard` | *Không* | *Không* |
| **Quản trị** | `AdminLayoutComponent` | `layout/admin-layout/admin-layout.component.ts` | `/admin` (Redirect sang `/admin/analytics`) | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminAnalyticsComponent` | `pages/admin/admin-analytics/admin-analytics.component.ts` | `/admin/analytics` | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminContentManagementComponent` | `pages/admin/admin-content-management/admin-content-management.component.html` | `/admin/content-management` | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminReportsComponent` | `pages/admin/admin-reports/admin-reports.component.ts` | `/admin/reports` | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminUserManagementComponent` | `pages/admin/admin-user-management/admin-user-management.component.ts` | `/admin/user-management` | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminSystemSettingsComponent` | `pages/admin/admin-system-settings/admin-system-settings.component.ts` | `/admin/system-settings` | Admin | `adminGuard` | *Không* | *Không* |
| **Quản trị** | `AdminArticleReviewComponent` | `pages/admin/admin-article-review/admin-article-review.component.ts` | `/admin/articles/review/:id` | Admin | `adminGuard` | `:id` (ID bài viết duyệt) | *Không* |

---

## 3. Các Cách Viết URL Trong HTML Template (Template Navigation)

Trong mã nguồn Angular của dự án, có 6 mẫu cú pháp điều hướng chính trên giao diện template:

### 3.1. Cú pháp chuỗi tĩnh (Static String)
Áp dụng cho các đường dẫn cố định không có tham số biến thiên:
```html
<!-- Chuyển về trang chủ -->
<a routerLink="/">Trang chủ</a>

<!-- Điều hướng trang Khám phá / Tìm kiếm -->
<a routerLink="/search">Khám phá</a>

<!-- Chuyển đến trang Viết bài mới -->
<a routerLink="/blog/creation">Viết bài</a>

<!-- Chuyển đến trang Cài đặt -->
<a routerLink="/settings">Cài đặt</a>

<!-- Chuyển đến Trang cá nhân -->
<a routerLink="/profile">Trang cá nhân</a>

<!-- Chuyển đến Trang quản trị -->
<a routerLink="/admin">Trang quản trị</a>
```

### 3.2. Cú pháp mảng tham số động (Dynamic Array Notation)
Áp dụng khi đường dẫn có chứa tham số đường dẫn (Path Parameter như `id`):
```html
<!-- Điều hướng tới chi tiết bài viết theo id (Trong BlogCardComponent) -->
<a [routerLink]="['/blog/detail', post.id]">
  {{ post.title }}
</a>

<!-- Điều hướng tới trang Admin kiểm duyệt bài viết cụ thể -->
<a [routerLink]="['/admin/articles/review', blog.id]">
  Xem xét duyệt
</a>

<!-- Điều hướng tới trang chỉnh sửa bài viết của tác giả -->
<a [routerLink]="['/blog/edit', blog.id]">
  Chỉnh sửa bài viết
</a>
```

### 3.3. Cú pháp điều hướng theo điều kiện dữ liệu (Ternary Expression)
Áp dụng khi mục tiêu điều hướng phụ thuộc vào trạng thái bài viết (ví dụ: đã xuất bản thì xem chi tiết, bản nháp/chờ duyệt thì vào trang sửa):
```html
<!-- Trong ProfilePostsComponent -->
<a [routerLink]="blog.status === BlogStatus.PUBLISHED ? ['/blog/detail', blog.id] : ['/blog/edit', blog.id]">
  <h4>{{ blog.title }}</h4>
</a>
```

### 3.4. Quản lý trạng thái Active với `routerLinkActive`
Để đánh dấu trực quan thẻ menu nào đang được kích hoạt:
```html
<!-- Trường hợp so khớp chính xác đường dẫn gốc (exact match) -->
<a 
  routerLink="/" 
  routerLinkActive="!text-accent-rust !font-bold !bg-accent-rust/10"
  [routerLinkActiveOptions]="{ exact: true }"
>
  Trang chủ
</a>

<!-- Trường hợp so khớp đường dẫn phân cấp (prefix match) -->
<a 
  routerLink="/settings/profile" 
  routerLinkActive="bg-accent-rust/10 text-accent-rust font-semibold"
>
  Trang cá nhân
</a>
```

### 3.5. Sử dụng Template Reference Variable (`#rla="routerLinkActive"`)
Kỹ thuật này được dùng phổ biến trong `AdminLayoutComponent` và `MobileNavbarComponent` để thay đổi thuộc tính SVG hoặc áp dụng class động dựa trên trạng thái `isActive`:
```html
<a
  routerLink="/admin/analytics"
  routerLinkActive
  #rlaDashboard="routerLinkActive"
  [class.text-accent-rust]="rlaDashboard.isActive"
  [class.bg-primary-container]="rlaDashboard.isActive"
>
  <!-- Kích hoạt thuộc tính fill cho Material Symbol khi tab đang chọn -->
  <span 
    class="material-symbols-outlined" 
    [class.fill-icon]="rlaDashboard.isActive"
  >
    dashboard
  </span>
  <span>Dashboard</span>
</a>
```

### 3.6. Truyền Query Parameters trực tiếp trong Template
```html
<!-- Chuyển đến trang login kèm returnUrl khi chưa xác thực -->
<a routerLink="/login" [queryParams]="{ returnUrl: '/blog/creation' }">
  Đăng nhập để viết bài
</a>
```

---

## 4. Các Cách Viết & Điều Hướng URL Trong TypeScript (Programmatic Navigation)

Trong code logic (`.ts`), việc điều hướng được thực hiện thông qua dịch vụ `Router`:

### 4.1. Điều hướng theo mảng segment (`Router.navigate`)
```typescript
import { inject } from '@angular/core';
import { Router } from '@angular/router';

export class ExampleComponent {
  private readonly router = inject(Router);

  // 1. Điều hướng đến trang tĩnh
  goToHome() {
    this.router.navigate(['/']);
  }

  // 2. Điều hướng kèm Path Parameter
  viewBlog(blogId: number) {
    this.router.navigate(['/blog/detail', blogId]);
  }

  // 3. Điều hướng kèm Query Parameters (như truyền reset token)
  redirectToResetPassword(token: string) {
    this.router.navigate(['/auth/reset-password'], { 
      queryParams: { token: token } 
    });
  }
}
```

### 4.2. Điều hướng theo chuỗi URL hoàn chỉnh (`Router.navigateByUrl`)
Thích hợp khi URL là một chuỗi đầy đủ được trả về từ Backend hoặc lưu tạm thời:
```typescript
// 1. Điều hướng sau khi Login thành công về returnUrl
const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
this.router.navigateByUrl(returnUrl);

// 2. Điều hướng khi click vào thông báo In-App (NotificationDropdownComponent)
onNotificationClick(item: NotificationResponse): void {
  if (item.relatedUrl) {
    this.router.navigateByUrl(item.relatedUrl);
  }
}
```

### 4.3. Chuyển hướng trong Route Guards (`createUrlTree`)
Trong `authGuard` và `adminGuard`, cơ chế chuyển hướng trả về một `UrlTree`:
```typescript
// auth.guard.ts: Tự động đính kèm returnUrl khi người dùng chưa đăng nhập
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.currentUser()) {
    return true;
  }
  return router.createUrlTree(['/login'], { 
    queryParams: { returnUrl: state.url } 
  });
};
```

### 4.4. Quay lại trang trước đó (`Location.back`)
Được dùng trong màn hình kiểm duyệt bài viết của Admin (`AdminArticleReviewComponent`):
```typescript
import { Location } from '@angular/common';

export class AdminArticleReviewComponent {
  private readonly location = inject(Location);

  goBack(): void {
    this.location.back();
  }
}
```

---

## 5. Cách Đọc Tham Số URL (Parameters) Trong Component

### 5.1. Đọc Path Parameter (`:id`)
Có hai cách tiếp cận:

#### Cách 1: Đọc tức thời (Snapshot) — Thích hợp khi component luôn bị hủy và tạo lại khi đổi URL:
```typescript
import { inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

export class AdminArticleReviewComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadArticle(id);
    }
  }
}
```

#### Cách 2: Lắng nghe Reactive (Observable) — Bắt buộc khi cùng component nhưng URL thay đổi tham số (ví dụ: chuyển từ bài viết này sang bài viết khác):
```typescript
// Trong BlogDetailAuthorComponent:
ngOnInit(): void {
  this.route.params.subscribe((params) => {
    const id = params['id'];
    if (id) {
      this.fetchBlogDetail(id);
    }
  });
}
```

### 5.2. Đọc Query Parameter (`?token=...`, `?returnUrl=...`)
```typescript
// 1. Đọc token kích hoạt tài khoản (ActiveAccountComponent)
ngOnInit(): void {
  const token = this.route.snapshot.queryParamMap.get('token');
  if (token) {
    this.authService.activeAccount(token).subscribe(...);
  }
}

// 2. Đọc token đặt lại mật khẩu (ResetPasswordComponent)
ngOnInit(): void {
  const tokenFromUrl = this.route.snapshot.queryParamMap.get('token');
  if (tokenFromUrl) {
    this.token.set(tokenFromUrl);
  }
}
```

---

## 6. Bản Đồ Ánh Xạ URL: Frontend Component <-> Backend REST API

Khi một Component trên giao diện tương tác với người dùng, nó sẽ thông qua các Angular Service để gửi request đến các API Endpoint phía Backend Spring Boot (`http://localhost:8080`):

```
┌──────────────────────────────────────┐          ┌─────────────────────────────────────────┐
│     Frontend Component & Route       │  HTTP    │        Backend Spring Controller        │
│    (Angular Pages & Templates)       │ ───────> │         (RESTful Endpoints)             │
└──────────────────────────────────────┘          └─────────────────────────────────────────┘
```

| Domain | Frontend Route | Frontend Component | Backend API Endpoint | HTTP Method | Spring Controller |
|---|---|---|---|---|---|
| **Identity** | `/login` | `LoginComponent` | `/api/v1/auth/login` | `POST` | `AuthController` |
| **Identity** | `/login` | `LoginComponent` | `/api/v1/auth/login/social?provider={name}` | `GET` | `AuthController` |
| **Identity** | `/register` | `RegisterComponent` | `/api/v1/auth/register` | `POST` | `AuthController` |
| **Identity** | `/auth/active-account` | `ActiveAccountComponent` | `/api/v1/auth/activeAccount?token={token}` | `GET` | `AuthController` |
| **Identity** | `/auth/forgot-password` | `ForgotPasswordComponent` | `/api/v1/auth/forgot-password` | `POST` | `AuthController` |
| **Identity** | `/auth/forgot-password` | `ForgotPasswordComponent` | `/api/v1/auth/verify-otp` | `POST` | `AuthController` |
| **Identity** | `/auth/reset-password` | `ResetPasswordComponent` | `/api/v1/auth/reset-password` | `POST` | `AuthController` |
| **Identity** | `/auth/change-password` | `ChangePasswordComponent` | `/api/v1/auth/change-password` | `POST` | `AuthController` |
| **Identity** | `/settings/security` | `SecuritySettingsComponent` | `/api/v1/auth/change-password` | `POST` | `AuthController` |
| **Identity** | `/profile/update` | `UpdateProfileComponent` | `/api/v1/auth/profile` | `GET`, `PUT` | `AuthController` |
| **Content** | `/` | `HomeComponent` | `/api/v1/stats/home` | `GET` | `HomeStatsController` |
| **Content** | `/` | `HomeComponent` | `/api/v1/blogs?page=0&size=6` | `GET` | `BlogController` |
| **Content** | `/search` | `SearchCategoryGridComponent` | `/api/v1/categories` | `GET` | `CategoryController` |
| **Content** | `/search` | `SearchCategoryGridComponent` | `/api/v1/blogs?keyword=...` | `GET` | `BlogController` |
| **Content** | `/blog/creation` | `BlogCreationSplitComponent` | `/api/v1/blogs` | `POST` | `BlogController` |
| **Content** | `/blog/edit/:id` | `BlogCreationSplitComponent` | `/api/v1/blogs/{id}` | `GET`, `PUT` | `BlogController` |
| **Content** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/blogs/{id}` | `GET` | `BlogController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/likes/toggle` | `POST` | `LikeController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/bookmarks/toggle` | `POST` | `BookmarkController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/comments/blog/{blogId}` | `GET` | `CommentController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/comments` | `POST` | `CommentController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/views` | `POST` | `ViewController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/shares` | `POST` | `ShareController` |
| **Interactions** | `/blog/detail/:id` | `BlogDetailAuthorComponent` | `/api/v1/users/{authorId}/follow/toggle`| `POST` | `FollowController` |
| **Storage** | `/blog/creation` | `BlogCreationSplitComponent` | `/api/v1/file/upload-url` | `POST` | `FileController` |
| **Storage** | `/blog/creation` | `BlogCreationSplitComponent` | `/api/v1/blogs/images/activate` | `POST` | `BlogController` |
| **Profile** | `/profile/posts` | `ProfilePostsComponent` | `/api/v1/blogs?userId={id}&status={st}`| `GET` | `BlogController` |
| **Profile** | `/profile/saved` | `ProfileSavedComponent` | `/api/v1/bookmarks/my-bookmarks` | `GET` | `BookmarkController` |
| **Notification**| `HeaderComponent` | `NotificationDropdownComponent` | `/api/v1/notifications` | `GET` | `NotificationController` |
| **Notification**| `HeaderComponent` | `NotificationDropdownComponent` | `/api/v1/notifications/unread-count` | `GET` | `NotificationController` |
| **Notification**| `HeaderComponent` | `NotificationDropdownComponent` | `/api/v1/notifications/{id}/read` | `PUT` | `NotificationController` |
| **Notification**| `HeaderComponent` | `NotificationDropdownComponent` | `/api/v1/notifications/read-all` | `PUT` | `NotificationController` |
| **Settings** | `/settings/notifications`| `NotificationSettingsComponent` | `/api/v1/settings/notifications` | `GET`, `PUT` | `NotificationSettingController` |
| **Admin** | `/admin/analytics` | `AdminAnalyticsComponent` | `/api/v1/admin/dashboard/stats` | `GET` | `AdminDashboardController` |
| **Admin** | `/admin/content-management` | `AdminContentManagementComponent`| `/api/v1/admin/blogs` | `GET` | `AdminBlogController` |
| **Admin** | `/admin/articles/review/:id`| `AdminArticleReviewComponent` | `/api/v1/admin/blogs/{id}/approve` | `PUT` | `AdminBlogController` |
| **Admin** | `/admin/articles/review/:id`| `AdminArticleReviewComponent` | `/api/v1/admin/blogs/{id}/reject` | `PUT` | `AdminBlogController` |
| **Admin** | `/admin/reports` | `AdminReportsComponent` | `/api/v1/admin/reports` | `GET` | `AdminReportController` |
| **Admin** | `/admin/reports` | `AdminReportsComponent` | `/api/v1/admin/reports/{id}/resolve` | `PUT` | `AdminReportController` |
| **Admin** | `/admin/user-management`| `AdminUserManagementComponent` | `/api/v1/admin/users` | `GET` | `AdminUserController` |
| **Admin** | `/admin/system-settings`| `AdminSystemSettingsComponent` | `/api/v1/admin/settings` | `GET`, `PUT` | `AdminSystemSettingsController` |

---

## 7. Quy Ước & Tiêu Chuẩn Viết URL (Best Practices)

Để đảm bảo tính nhất quán và khả năng bảo trì khi mở rộng dự án, toàn bộ URL cần tuân thủ các quy chuẩn sau:

### 7.1. Định dạng Kebab-case cho URL Segments
- **Quy tắc**: Toàn bộ đường dẫn URL chỉ dùng **chữ thường** và dấu gạch nối ngang `-` để ngăn cách các từ. Tuyệt đối không dùng chữ hoa, `camelCase` hay `snake_case`.
- **Đúng**: `/admin/content-management`, `/admin/system-settings`, `/auth/forgot-password`, `/auth/change-password`.
- **Sai**: `/admin/contentManagement`, `/admin/system_settings`, `/auth/ForgotPassword`.

### 7.2. Định dạng camelCase cho Query Parameters
- **Quy tắc**: Các tham số sau dấu `?` trong URL nên viết theo dạng `camelCase`.
- **Ví dụ**: `?returnUrl=/blog/creation`, `?token=abc-123-xyz`, `?userId=42`.

### 7.3. Phân cấp định danh tài nguyên (Resource-Based Hierarchy)
- **Cấu trúc chuẩn**: `/{phân-hệ-chính}/{tài-nguyên}/{hành-động-hoặc-id}`
- **Ví dụ**:
  - Xem chi tiết bài viết: `/blog/detail/:id`
  - Sửa bài viết: `/blog/edit/:id`
  - Soạn bài mới: `/blog/creation`
  - Quản trị duyệt bài: `/admin/articles/review/:id`

### 7.4. Không sử dụng dấu gạch chéo cuối dòng (No Trailing Slash)
- Giữ đường dẫn gọn gàng: Dùng `/search` thay vì `/search/`, `/login` thay vì `/login/`.

---

## 8. Các Lỗi Sai URL Đã Phát Hiện Cần Chú Ý Trong Source Code

Trong quá trình rà soát mã nguồn thực tế của dự án, có một số điểm không đồng bộ giữa khai báo Route và liên kết trên giao diện cần đặc biệt lưu ý:

### ⚠️ Điểm 1: Không nhất quán giữa `/blog/create` và `/blog/creation`
- **Hiện trạng**: 
  - Trong `blog.routes.ts`: Khai báo là `path: 'creation'` -> URL hợp lệ là `/blog/creation`.
  - Trong `profile-posts.component.html` (dòng 50) và `profile-drafts.component.html` (dòng 4): Đang viết `<a routerLink="/blog/create">`.
- **Hệ quả**: Bấm vào nút "Viết bài mới" từ trang cá nhân sẽ dẫn đến trang lỗi 404 hoặc không điều hướng được.
- **Khắc phục**: Đồng bộ toàn bộ liên kết thành `/blog/creation` (hoặc cấu hình alias redirect trong `blog.routes.ts`: `{ path: 'create', redirectTo: 'creation', pathMatch: 'full' }`).

### ⚠️ Điểm 2: Sai định dạng URL xem bài viết trong Trang Đã Lưu (`profile-saved`)
- **Hiện trạng**:
  - Trong `profile-saved.component.html` (dòng 30): Đang viết `<a [routerLink]="['/blog', blog.id]">`.
  - Trong `blog.routes.ts`: Định nghĩa là `path: 'detail/:id'` (tương đương `/blog/detail/:id`).
- **Hệ quả**: Khi người dùng click vào bài viết đã bookmark từ trang profile, router sẽ không tìm thấy route `/blog/:id`.
- **Khắc phục**: Sửa thành `<a [routerLink]="['/blog/detail', blog.id]">`.

### ⚠️ Điểm 3: Route `profile/drafts` bị chuyển hướng về `posts`
- **Hiện trạng**: Trong `profile.routes.ts`, route `drafts` đang được cấu hình: `{ path: 'drafts', redirectTo: 'posts', pathMatch: 'full' }` dù component `ProfileDraftsComponent` đã được xây dựng sẵn.
- **Khắc phục**: Nếu muốn kích hoạt trang xem bài nháp riêng biệt, cập nhật `profile.routes.ts` để nạp `ProfileDraftsComponent`.

---

## 9. Hướng Dẫn Mẫu: 5 Bước Thêm Mới Một Component & Đăng Ký URL Chuẩn

Khi cần phát triển một chức năng mới (Ví dụ: Thêm trang "Chi tiết danh mục" — `CategoryDetailComponent` với URL `/category/:slug`):

### Bước 1: Tạo Component Standalone
```bash
# Chạy trong thư mục frontend
npx ng g c pages/category/category-detail --standalone
```

### Bước 2: Khai báo Route trong file routes tương ứng
Mở `app.routes.ts` (hoặc feature routes) và đăng ký:
```typescript
{
  path: 'category/:slug',
  loadComponent: () =>
    import('./pages/category/category-detail/category-detail.component').then(
      (m) => m.CategoryDetailComponent
    ),
  canActivate: [/* authGuard nếu cần */]
}
```

### Bước 3: Đọc tham số trong Component TypeScript
```typescript
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-category-detail',
  standalone: true,
  templateUrl: './category-detail.component.html',
})
export class CategoryDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  categorySlug = '';

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.categorySlug = params['slug'];
      // Gọi service lấy dữ liệu theo slug
    });
  }
}
```

### Bước 4: Viết liên kết điều hướng từ Template khác
```html
<a [routerLink]="['/category', category.slug]" class="font-medium text-clay-600">
  {{ category.name }}
</a>
```

### Bước 5: Viết điều hướng từ TypeScript (nếu xử lý bằng hàm)
```typescript
onCategorySelect(slug: string): void {
  this.router.navigate(['/category', slug]);
}
```

---
*Tài liệu này được biên soạn cho toàn bộ đội ngũ phát triển Frontend và Backend của dự án Blog-Website.*
