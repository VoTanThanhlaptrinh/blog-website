# 📋 API Test Cases — Blog Website Backend

> **Base URL:** `http://localhost:8080`
> **Authentication:** JWT token trong Cookie `token` (HttpOnly, Lax)
> **Response chung:** `{ "data": T, "message": "string", "code": int }`

---

## Mục lục

1. [Health Check](#1-health-check-api)
2. [Authentication (Identity)](#2-authentication-api)
3. [Blog (Content)](#3-blog-api)
4. [Category](#4-category-api)
5. [Comment](#5-comment-api)
6. [Like](#6-like-api)
7. [Bookmark](#7-bookmark-api)
8. [Follow](#8-follow-api)
9. [Share](#9-share-api)
10. [View](#10-view-api)
11. [Notification](#11-notification-api)
12. [Notification Setting](#12-notification-setting-api)
13. [File Upload (Storage)](#13-file-upload-api)
14. [Statistics](#14-statistics-api)
15. [Report (User)](#15-report-api-user)
16. [Admin — Blog Management](#16-admin-blog-management-api)
17. [Admin — User Management](#17-admin-user-management-api)
18. [Admin — Report Management](#18-admin-report-management-api)
19. [Admin — Dashboard](#19-admin-dashboard-api)
20. [Admin — System Settings](#20-admin-system-settings-api)
21. [Admin — Seed Data](#21-admin-seed-data-api)

---

## Quy ước

| Ký hiệu | Ý nghĩa |
|----------|----------|
| 🟢 | Test case **thành công** (Happy path) |
| 🔴 | Test case **thất bại** (Negative / Error) |
| 🟡 | Test case **biên** (Edge case / Boundary) |
| 🔒 | Test case **bảo mật / phân quyền** (Security) |

---

## 1. Health Check API

### `GET /api/v1/healthcheck`

| # | Loại | Tên test case | Mô tả | Request | Expected Response |
|---|------|---------------|-------|---------|-------------------|
| 1.1 | 🟢 | Health check thành công | Kiểm tra server hoạt động | `GET /api/v1/healthcheck` | `200 OK`, body: `"OK"` |
| 1.2 | 🟡 | Gọi bằng phương thức POST | Gửi POST thay vì GET | `POST /api/v1/healthcheck` | `405 Method Not Allowed` |

---

## 2. Authentication API

### 2.1 `POST /api/v1/auth/login` — Đăng nhập

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.1.1 | 🟢 | Đăng nhập thành công | Email và password đúng | `{"email":"user@test.com","password":"123456"}` | `200`, data chứa `accessToken`, `refreshToken`, `user` |
| 2.1.2 | 🟢 | Cookie token được set | Đăng nhập thành công, cookie `token` được set | (như trên) | Response header có `Set-Cookie: token=...;HttpOnly;Path=/;SameSite=Lax` |
| 2.1.3 | 🔴 | Email rỗng | Gửi email = null | `{"email":null,"password":"123456"}` | `400`, message: `"email bi rong"` |
| 2.1.4 | 🔴 | Password rỗng | Gửi password = null | `{"email":"user@test.com","password":null}` | `400`, message: `"password bi rong"` |
| 2.1.5 | 🔴 | Email sai định dạng | Gửi email không phải email | `{"email":"not-an-email","password":"123456"}` | `400`, message: `"Khong phai email"` |
| 2.1.6 | 🔴 | Sai mật khẩu | Email đúng, password sai | `{"email":"user@test.com","password":"wrong"}` | `401` hoặc error response tương ứng |
| 2.1.7 | 🔴 | Email không tồn tại | Email chưa đăng ký | `{"email":"noexist@test.com","password":"123456"}` | `404` hoặc error tương ứng (UserNotFoundException) |
| 2.1.8 | 🔴 | Tài khoản chưa kích hoạt | Email đã đăng ký nhưng chưa active | (email chưa active) | Error tương ứng |
| 2.1.9 | 🔴 | Tài khoản bị khóa | Tài khoản có status = BANNED | (email bị ban) | Error tương ứng |
| 2.1.10 | 🔴 | Body trống | Gửi request body rỗng | `{}` | `400` |
| 2.1.11 | 🟡 | Email có khoảng trắng đầu/cuối | `" user@test.com "` | `{"email":" user@test.com ","password":"123456"}` | Tùy logic: `400` hoặc trim rồi login |

---

### 2.2 `GET /api/v1/auth/login/social` — Lấy URL đăng nhập mạng xã hội

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 2.2.1 | 🟢 | Lấy danh sách URL thành công | Không cần auth | `200`, data: `{"google":"/oauth2/authorization/google","facebook":"/oauth2/authorization/facebook"}` |

---

### 2.3 `POST /api/v1/auth/register` — Đăng ký

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.3.1 | 🟢 | Đăng ký thành công | Email hợp lệ, password và confirm khớp | `{"email":"new@test.com","password":"Abc123","confirmPassword":"Abc123"}` | `200`, message: `"Dang ky thanh cong"` |
| 2.3.2 | 🔴 | Email rỗng | email = null | `{"email":null,...}` | `400`, `"email bi rong"` |
| 2.3.3 | 🔴 | Email sai định dạng | Không phải email | `{"email":"abc","password":"123","confirmPassword":"123"}` | `400`, `"Khong phai email"` |
| 2.3.4 | 🔴 | Password rỗng | password = null | `{...,"password":null,...}` | `400`, `"password bi rong"` |
| 2.3.5 | 🔴 | Confirm password rỗng | confirmPassword = null | `{...,"confirmPassword":null}` | `400`, `"confirm password bi rong"` |
| 2.3.6 | 🔴 | Password không khớp confirm | Hai password không khớp | `{"email":"a@b.com","password":"123","confirmPassword":"456"}` | `400`, `"password va confirm phai giong nhau"` |
| 2.3.7 | 🔴 | Email đã tồn tại | Đăng ký email đã dùng | (email đã đăng ký) | Error: `UsernameAlreadyExistsException` |
| 2.3.8 | 🟡 | Password rất dài | Password > 100 ký tự | (password rất dài) | `200` hoặc giới hạn tùy logic |
| 2.3.9 | 🟡 | Email Unicode | Email có ký tự Unicode | `{"email":"user@test.com",...}` | `400` hoặc chấp nhận tùy config |

---

### 2.4 `GET /api/v1/auth/profile` — Xem profile

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 2.4.1 | 🟢 | Xem profile thành công | Đăng nhập hợp lệ | `200`, data chứa: `id, email, phone, birthDate, avatarUrl, bio, roles` |
| 2.4.2 | 🔒 | Không có token | Gọi API không có cookie token | `401 Unauthorized` |
| 2.4.3 | 🔒 | Token hết hạn | Cookie token đã expire | `401 Unauthorized` |
| 2.4.4 | 🔒 | Token không hợp lệ | Cookie chứa token sai | `401 Unauthorized` |

---

### 2.5 `PUT /api/v1/auth/profile` — Cập nhật profile

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.5.1 | 🟢 | Cập nhật đầy đủ | Gửi tất cả trường | `{"phone":"0123456789","birthDate":"2000-01-01","avatarUrl":"https://img.com/a.jpg","bio":"Hello"}` | `200`, trả về profile cập nhật |
| 2.5.2 | 🟢 | Cập nhật một phần | Chỉ gửi bio | `{"bio":"New bio"}` | `200` |
| 2.5.3 | 🔒 | Không có token | Không đăng nhập | — | `401` |
| 2.5.4 | 🟡 | Bio rất dài | Bio > 1000 ký tự | (bio rất dài) | Tùy validation |
| 2.5.5 | 🟡 | birthDate không hợp lệ | Format sai | `{"birthDate":"not-a-date"}` | `400` |

---

### 2.7 `POST /api/v1/auth/forgot-password` — Quên mật khẩu

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.7.1 | 🟢 | Gửi OTP thành công | Email tồn tại | `{"email":"user@test.com"}` | `200`, `"Đã gửi mã OTP đến email của bạn"` |
| 2.7.2 | 🔴 | Email không tồn tại | Email chưa đăng ký | `{"email":"no@test.com"}` | Error tương ứng (UserNotFoundException) |
| 2.7.3 | 🔴 | Email rỗng | email = null | `{"email":null}` | `400` hoặc error tương ứng |
| 2.7.4 | 🟡 | Gửi nhiều lần liên tục | Gọi API liên tục 5 lần | (cùng email) | Có thể giới hạn rate hoặc trả OTP mới |

---

### 2.8 `POST /api/v1/auth/verify-otp` — Xác thực OTP

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.8.1 | 🟢 | Xác thực thành công | OTP đúng | `{"email":"user@test.com","otp":"123456"}` | `200`, data chứa reset token |
| 2.8.2 | 🔴 | OTP sai | OTP không khớp | `{"email":"user@test.com","otp":"000000"}` | Error: `InvalidOtpException` |
| 2.8.3 | 🔴 | OTP hết hạn | OTP đã expire | (otp cũ) | Error: `ExpiredOtpException` |
| 2.8.4 | 🔴 | Email rỗng | email = "" | `{"email":"","otp":"123456"}` | `400` (@NotBlank) |
| 2.8.5 | 🔴 | OTP rỗng | otp = "" | `{"email":"user@test.com","otp":""}` | `400` (@NotBlank) |

---

### 2.9 `POST /api/v1/auth/reset-password` — Đặt lại mật khẩu

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.9.1 | 🟢 | Đặt lại thành công | Token hợp lệ, password và confirm khớp | `{"token":"valid-token","newPassword":"NewPass1","confirmPassword":"NewPass1"}` | `200`, `"Đặt lại mật khẩu thành công"` |
| 2.9.2 | 🔴 | Token rỗng | token = "" | `{"token":"","newPassword":"a","confirmPassword":"a"}` | `400` (@NotBlank) |
| 2.9.3 | 🔴 | Token không hợp lệ | Token sai | `{"token":"invalid",...}` | Error: `InvalidResetTokenException` |
| 2.9.4 | 🔴 | newPassword rỗng | newPassword = "" | `{...,"newPassword":"",...}` | `400` (@NotBlank) |
| 2.9.5 | 🔴 | confirmPassword rỗng | confirmPassword = "" | `{...,"confirmPassword":""}` | `400` (@NotBlank) |
| 2.9.6 | 🔴 | Password không khớp | newPassword khác confirmPassword | `{...,"newPassword":"A","confirmPassword":"B"}` | Error: `PasswordMismatchException` |

---

### 2.10 `POST /api/v1/auth/change-password` — Đổi mật khẩu

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.10.1 | 🟢 | Đổi mật khẩu thành công | Mật khẩu cũ đúng | `{"oldPassword":"123456","newPassword":"654321"}` | `200`, `"Đổi mật khẩu thành công"` |
| 2.10.2 | 🔴 | Mật khẩu cũ sai | oldPassword sai | `{"oldPassword":"wrong","newPassword":"new"}` | Error: `IncorrectPasswordException` |
| 2.10.3 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 2.10.4 | 🟡 | newPassword = oldPassword | Mật khẩu mới giống cũ | `{"oldPassword":"123","newPassword":"123"}` | Tùy logic |

---

### 2.11 `POST /api/v1/auth/logout` — Đăng xuất

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 2.11.1 | 🟢 | Đăng xuất thành công | Clear cookie token | `200`, cookie `token` được xóa (maxAge=0) |
| 2.11.2 | 🟡 | Đăng xuất khi chưa login | Không có token | `200` (vẫn xóa cookie) |

---

### 2.12 `GET /api/v1/auth/activeAccount` — Kích hoạt tài khoản

| # | Loại | Tên test case | Mô tả | Query Param | Expected |
|---|------|---------------|-------|-------------|----------|
| 2.12.1 | 🟢 | Kích hoạt thành công | Token hợp lệ | `?token=valid-token` | `200`, `"Kích hoạt thành công"` |
| 2.12.2 | 🔴 | Token không hợp lệ | Token sai hoặc hết hạn | `?token=invalid` | Error: `InvalidTokenException` |
| 2.12.3 | 🔴 | Tài khoản đã kích hoạt | Active lại lần 2 | `?token=used-token` | Error: `AccountAlreadyActiveException` |
| 2.12.4 | 🔴 | Thiếu token param | Không gửi token | `GET /api/v1/auth/activeAccount` | `400` |

---

### 2.13 `PUT /api/v1/auth/deactivate` — Vô hiệu hóa tài khoản

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 2.13.1 | 🟢 | Vô hiệu hóa thành công | User đang active | `200`, `"Vô hiệu hóa tài khoản thành công"` |
| 2.13.2 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 2.13.3 | 🟡 | Vô hiệu hóa khi đã inactive | Gọi lại khi đã deactivate | Tùy logic |

---

## 3. Blog API

### 3.1 `POST /api/v1/blogs` — Tạo bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 3.1.1 | 🟢 | Tạo bài viết DRAFT thành công | Đầy đủ thông tin | `{"title":"Tiêu đề","description":"Mô tả","content":"Nội dung","status":"DRAFT","categoryId":1,"thumbnailUrl":"https://img.com/t.jpg"}` | `200`, trả về BlogResponse |
| 3.1.2 | 🟢 | Tạo bài viết PENDING | Status = PENDING để chờ duyệt | (như trên, status PENDING) | `200` |
| 3.1.3 | 🔴 | Tiêu đề rỗng | title = "" | `{"title":"","description":"abc","content":"xyz"}` | `400`, `"Tiêu đề không được để trống"` |
| 3.1.4 | 🔴 | Mô tả rỗng | description = "" | `{"title":"abc","description":"","content":"xyz"}` | `400`, `"Mô tả không được để trống"` |
| 3.1.5 | 🔴 | Nội dung rỗng | content = "" | `{"title":"abc","description":"def","content":""}` | `400`, `"Nội dung không được để trống"` |
| 3.1.6 | 🔴 | Tiêu đề quá dài | title > 255 ký tự | (title 256 chars) | `400`, `"Tiêu đề tối đa 255 ký tự"` |
| 3.1.7 | 🔴 | Mô tả quá dài | description > 1000 ký tự | (desc 1001 chars) | `400`, `"Mô tả tối đa 1000 ký tự"` |
| 3.1.8 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 3.1.9 | 🟡 | Không gửi categoryId | categoryId = null | (bỏ categoryId) | `200` (categoryId optional) |
| 3.1.10 | 🟡 | categoryId không tồn tại | categoryId = 999999 | (id không tồn tại) | Error tương ứng |
| 3.1.11 | 🟡 | Tiêu đề đúng 255 ký tự | Boundary test | (title 255 chars) | `200` |
| 3.1.12 | 🟡 | Mô tả đúng 1000 ký tự | Boundary test | (desc 1000 chars) | `200` |

---

### 3.2 `GET /api/v1/blogs/{id}` — Xem chi tiết bài viết

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 3.2.1 | 🟢 | Xem bài viết thành công | Blog tồn tại, đã PUBLISHED | `200`, trả BlogResponse đầy đủ |
| 3.2.2 | 🟢 | Xem bài PUBLISHED không cần login | Public access | `200` |
| 3.2.3 | 🟢 | Xem bài có đăng nhập | Trả thêm `likedByCurrentUser` | `200`, `likedByCurrentUser` = true/false |
| 3.2.4 | 🔴 | Blog không tồn tại | id = 999999 | Error: `BlogNotFoundException` |
| 3.2.5 | 🔴 | ID không hợp lệ | id = "abc" | `400` |
| 3.2.6 | 🟡 | ID = 0 | Boundary | `404` hoặc error |
| 3.2.7 | 🟡 | ID số âm | id = -1 | `404` hoặc error |

---

### 3.3 `GET /api/v1/blogs` — Lấy danh sách bài viết (phân trang)

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 3.3.1 | 🟢 | Lấy danh sách mặc định | Không có filter | — | `200`, PageResponse với content, pageNumber, pageSize, totalElements... |
| 3.3.2 | 🟢 | Tìm theo keyword | keyword = "Spring" | `?keyword=Spring` | `200`, kết quả chứa "Spring" |
| 3.3.3 | 🟢 | Lọc theo status | status = PUBLISHED | `?status=PUBLISHED` | `200`, tất cả bài có status PUBLISHED |
| 3.3.4 | 🟢 | Lọc theo userId | userId = 1 | `?userId=1` | `200`, bài viết của user 1 |
| 3.3.5 | 🟢 | Lọc theo categoryId | categoryId = 2 | `?categoryId=2` | `200`, bài viết thuộc category 2 |
| 3.3.6 | 🟢 | Phân trang | page=1, size=5 | `?page=1&size=5` | `200`, pageNumber=1, pageSize=5 |
| 3.3.7 | 🟢 | Kết hợp nhiều filter | keyword + status + categoryId | `?keyword=Java&status=PUBLISHED&categoryId=1` | `200` |
| 3.3.8 | 🟡 | Page vượt quá dữ liệu | page = 9999 | `?page=9999` | `200`, content = [] |
| 3.3.9 | 🟡 | Size = 0 | Boundary | `?size=0` | `400` hoặc mặc định |
| 3.3.10 | 🟡 | Keyword rỗng | keyword = "" | `?keyword=` | `200`, trả tất cả |
| 3.3.11 | 🟡 | Status không hợp lệ | status = "INVALID" | `?status=INVALID` | `400` |

---

### 3.4 `GET /api/v1/blogs/search` — Tìm kiếm bài viết (cursor-based)

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 3.4.1 | 🟢 | Tìm kiếm lần đầu | Không có lastId | `?keyword=java&limit=10` | `200`, BlogCursorResponse: `{content, hasMore, nextCursor}` |
| 3.4.2 | 🟢 | Tìm kiếm trang tiếp | Có lastId từ trang trước | `?keyword=java&lastId=5&limit=10` | `200` |
| 3.4.3 | 🟢 | Lọc theo categories | Nhiều category | `?categories=Tech&categories=Java` | `200` |
| 3.4.4 | 🟡 | Limit = 0 | Boundary | `?limit=0` | Trả rỗng hoặc mặc định |
| 3.4.5 | 🟡 | Limit rất lớn | limit = 1000 | `?limit=1000` | `200`, giới hạn hoặc trả hết |
| 3.4.6 | 🟡 | Không có kết quả | Keyword không khớp | `?keyword=xyzxyzxyz123` | `200`, content = [], hasMore = false |

---

### 3.5 `GET /api/v1/blogs/me/cursor` — Bài viết cá nhân (cursor)

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 3.5.1 | 🟢 | Lấy bài viết cá nhân | User đã đăng nhập | — | `200`, BlogCursorResponse |
| 3.5.2 | 🟢 | Lọc theo status DRAFT | Chỉ lấy bài nháp | `?status=DRAFT` | `200`, tất cả bài DRAFT |
| 3.5.3 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 3.5.4 | 🟡 | User chưa có bài viết | User mới tạo | — | `200`, content = [] |

---

### 3.6 `GET /api/v1/blogs/me/thumbnails` — Danh sách ảnh bìa đã dùng

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 3.6.1 | 🟢 | Lấy danh sách ảnh thành công | User có bài viết | `200`, data: `List<String>` |
| 3.6.2 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 3.6.3 | 🟡 | Chưa có bài viết nào | User mới | `200`, data = [] |

---

### 3.7 `PUT /api/v1/blogs/{id}` — Cập nhật bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 3.7.1 | 🟢 | Cập nhật thành công | Sửa tiêu đề và nội dung | `{"title":"Tiêu đề mới","description":"Mô tả mới","content":"Nội dung mới","status":"DRAFT","categoryId":1}` | `200` |
| 3.7.2 | 🔴 | Tiêu đề rỗng | title = "" | (như createBlog) | `400` |
| 3.7.3 | 🔴 | Blog không tồn tại | id = 999999 | — | `BlogNotFoundException` |
| 3.7.4 | 🔒 | Sửa bài của người khác | User A sửa bài của User B | — | Error: `UnauthorizedBlogAccessException` |
| 3.7.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 3.7.6 | 🟡 | Chuyển status từ DRAFT sang PENDING | Gửi duyệt | `{...,"status":"PENDING"}` | `200` |
| 3.7.7 | 🔴 | Blog đã bị xóa | Blog status = DELETED | — | Error: `BlogAlreadyDeletedException` |

---

### 3.8 `DELETE /api/v1/blogs/{id}` — Xóa bài viết

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 3.8.1 | 🟢 | Xóa bài viết thành công | Owner xóa bài | `200`, `"Xóa bài viết thành công"` |
| 3.8.2 | 🔴 | Blog không tồn tại | id không tồn tại | `BlogNotFoundException` |
| 3.8.3 | 🔒 | Xóa bài của người khác | User A xóa bài User B | `UnauthorizedBlogAccessException` |
| 3.8.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 3.8.5 | 🟡 | Xóa bài đã xóa | Blog đã DELETED | `BlogAlreadyDeletedException` |

---

## 4. Category API

### 4.1 `GET /api/v1/categories` — Danh sách danh mục

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 4.1.1 | 🟢 | Lấy danh sách thành công | Public, không cần auth | `200`, `List<CategoryResponse>` với `id, name, slug, description` |
| 4.1.2 | 🟡 | Chưa có category nào | Database trống | `200`, data = [] |

---

### 4.2 `GET /api/v1/categories/{id}` — Chi tiết danh mục

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 4.2.1 | 🟢 | Lấy category thành công | ID tồn tại | `200`, CategoryResponse |
| 4.2.2 | 🔴 | ID không tồn tại | id = 999999 | Error tương ứng |

---

### 4.3 `POST /api/v1/categories` — Tạo danh mục

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 4.3.1 | 🟢 | Tạo danh mục thành công | name hợp lệ | `{"name":"Technology","description":"Tech blogs"}` | `200`, CategoryResponse |
| 4.3.2 | 🔴 | Tên rỗng | name = "" | `{"name":""}` | `400`, `"Tên danh mục không được để trống"` |
| 4.3.3 | 🟢 | Không gửi description | description optional | `{"name":"Science"}` | `200` |
| 4.3.4 | 🟡 | Tên trùng lặp | Category đã tồn tại | (tên đã có) | Tùy logic |

---

## 5. Comment API

### 5.1 `POST /api/v1/comments` — Tạo bình luận

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 5.1.1 | 🟢 | Bình luận gốc thành công | Không có parentId | `{"blogId":1,"content":"Bài viết hay!"}` | `200`, CommentResponse |
| 5.1.2 | 🟢 | Reply bình luận | Có parentId | `{"blogId":1,"content":"Đồng ý!","parentId":1}` | `200`, parentId = 1 |
| 5.1.3 | 🔴 | blogId rỗng | blogId = null | `{"blogId":null,"content":"abc"}` | `400`, `"Blog ID không được để trống"` |
| 5.1.4 | 🔴 | content rỗng | content = "" | `{"blogId":1,"content":""}` | `400`, `"Nội dung bình luận không được để trống"` |
| 5.1.5 | 🔴 | Blog không tồn tại | blogId = 999999 | `{"blogId":999999,"content":"abc"}` | `BlogNotFoundException` |
| 5.1.6 | 🔴 | Parent comment không tồn tại | parentId sai | `{"blogId":1,"content":"abc","parentId":999999}` | `ParentCommentNotFoundException` |
| 5.1.7 | 🔴 | Reply vào reply | Reply cấp 3 trở lên | (parent là reply) | `CannotReplyToReplyException` |
| 5.1.8 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

### 5.2 `PUT /api/v1/comments/{id}` — Sửa bình luận

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 5.2.1 | 🟢 | Sửa bình luận thành công | Owner sửa | `{"content":"Nội dung mới"}` | `200` |
| 5.2.2 | 🔴 | content rỗng | content = "" | `{"content":""}` | `400` |
| 5.2.3 | 🔴 | Comment không tồn tại | id sai | — | `CommentNotFoundException` |
| 5.2.4 | 🔒 | Sửa comment của người khác | User A sửa comment User B | — | `UnauthorizedCommentAccessException` |
| 5.2.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

### 5.3 `DELETE /api/v1/comments/{id}` — Xóa bình luận

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 5.3.1 | 🟢 | Xóa bình luận thành công | Owner xóa | `200` |
| 5.3.2 | 🔴 | Comment không tồn tại | id sai | `CommentNotFoundException` |
| 5.3.3 | 🔒 | Xóa comment người khác | Không phải owner | `UnauthorizedCommentAccessException` |
| 5.3.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 5.3.5 | 🟡 | Xóa comment đã xóa | Xóa lần 2 | `CommentAlreadyDeletedException` |

---

### 5.4 `GET /api/v1/comments/blog/{blogId}` — Danh sách bình luận theo blog

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 5.4.1 | 🟢 | Lấy comments thành công | Blog tồn tại | — | `200`, PageResponse<CommentResponse> với replies |
| 5.4.2 | 🟢 | Public access | Không cần login | — | `200` (permitAll trong SecurityConfig) |
| 5.4.3 | 🟢 | Phân trang | page=0, size=5 | `?page=0&size=5` | `200` |
| 5.4.4 | 🟢 | Xem khi đăng nhập | Trả `likedByCurrentUser` | — | `200`, có thông tin likedByCurrentUser |
| 5.4.5 | 🟡 | Blog không có comment | Blog mới | — | `200`, content = [] |
| 5.4.6 | 🔴 | Blog không tồn tại | blogId sai | — | Error hoặc content = [] |

---

## 6. Like API

### 6.1 `POST /api/v1/likes/toggle` — Like/Unlike bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 6.1.1 | 🟢 | Like bài viết (lần đầu) | Chưa like | `{"blogId":1}` | `200`, `liked=true`, `likesCount` tăng |
| 6.1.2 | 🟢 | Unlike bài viết (đã like) | Đã like rồi unlike | `{"blogId":1}` | `200`, `liked=false`, `likesCount` giảm |
| 6.1.3 | 🔴 | blogId rỗng | blogId = null | `{"blogId":null}` | `400`, `"Blog ID không được để trống"` |
| 6.1.4 | 🔴 | Blog không tồn tại | blogId sai | `{"blogId":999999}` | Error tương ứng |
| 6.1.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 6.1.6 | 🟡 | Toggle nhanh liên tục | Like/unlike nhiều lần | (gọi nhanh) | Đảm bảo nhất quán |

---

### 6.2 `POST /api/v1/likes/comment/toggle` — Like/Unlike bình luận

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 6.2.1 | 🟢 | Like comment lần đầu | Chưa like | `{"commentId":1}` | `200`, `liked=true`, `totalLikes` tăng |
| 6.2.2 | 🟢 | Unlike comment | Đã like rồi unlike | `{"commentId":1}` | `200`, `liked=false`, `totalLikes` giảm |
| 6.2.3 | 🔴 | commentId rỗng | commentId = null | `{"commentId":null}` | `400`, `"Comment ID không được để trống"` |
| 6.2.4 | 🔴 | Comment không tồn tại | commentId sai | `{"commentId":999999}` | Error |
| 6.2.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

## 7. Bookmark API

### 7.1 `POST /api/v1/bookmarks/toggle` — Lưu/Bỏ lưu bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 7.1.1 | 🟢 | Bookmark lần đầu | Chưa bookmark | `{"blogId":1}` | `200`, `bookmarked=true` |
| 7.1.2 | 🟢 | Bỏ bookmark | Đã bookmark rồi bỏ | `{"blogId":1}` | `200`, `bookmarked=false` |
| 7.1.3 | 🔴 | blogId rỗng | blogId = null | `{"blogId":null}` | `400`, `"Blog ID không được để trống"` |
| 7.1.4 | 🔴 | Blog không tồn tại | blogId sai | `{"blogId":999999}` | Error |
| 7.1.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

### 7.2 `GET /api/v1/bookmarks/me` — Danh sách bài viết đã lưu

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 7.2.1 | 🟢 | Lấy danh sách thành công | User có bookmark | — | `200`, PageResponse<BlogResponse> |
| 7.2.2 | 🟢 | Phân trang | page=0, size=5 | `?page=0&size=5` | `200` |
| 7.2.3 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 7.2.4 | 🟡 | Chưa bookmark gì | User mới | — | `200`, content = [] |

---

## 8. Follow API

### 8.1 `POST /api/v1/users/{userId}/follow` — Theo dõi/Bỏ theo dõi

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 8.1.1 | 🟢 | Follow thành công | Chưa follow | `200`, `isFollowing=true`, `followersCount` tăng |
| 8.1.2 | 🟢 | Unfollow thành công | Đã follow rồi unfollow | `200`, `isFollowing=false`, `followersCount` giảm |
| 8.1.3 | 🔴 | User không tồn tại | userId sai | Error |
| 8.1.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 8.1.5 | 🟡 | Tự follow chính mình | userId = currentUser.id | Tùy logic |

---

### 8.2 `GET /api/v1/users/{userId}/follow-status` — Trạng thái theo dõi

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 8.2.1 | 🟢 | Đang follow | isFollowing = true | `200`, FollowStatusResponse |
| 8.2.2 | 🟢 | Chưa follow | isFollowing = false | `200` |
| 8.2.3 | 🔴 | User không tồn tại | userId sai | Error |
| 8.2.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |

---

## 9. Share API

### 9.1 `POST /api/v1/shares` — Ghi nhận chia sẻ bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 9.1.1 | 🟢 | Chia sẻ thành công | Provider hợp lệ | `{"blogId":1,"provider":"facebook"}` | `200`, ShareResponse: `id, blogId, provider, totalShares, createdDate` |
| 9.1.2 | 🔴 | blogId rỗng | blogId = null | `{"blogId":null,"provider":"fb"}` | `400`, `"Blog ID không được để trống"` |
| 9.1.3 | 🔴 | provider rỗng | provider = "" | `{"blogId":1,"provider":""}` | `400`, `"Nền tảng chia sẻ (provider) không được để trống"` |
| 9.1.4 | 🔴 | Blog không tồn tại | blogId sai | `{"blogId":999999,"provider":"fb"}` | Error |
| 9.1.5 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 9.1.6 | 🟡 | Chia sẻ nhiều lần | Cùng blog, cùng provider | (gọi nhiều lần) | `200`, totalShares tăng |

---

## 10. View API

### 10.1 `POST /api/v1/views/record` — Ghi nhận lượt xem

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 10.1.1 | 🟢 | Ghi nhận thành công | blogId hợp lệ | `{"blogId":1}` | `200`, ViewResponse: `blogId, recorded=true, totalViews` |
| 10.1.2 | 🔴 | blogId rỗng | blogId = null | `{"blogId":null}` | `400`, `"Blog ID không được để trống"` |
| 10.1.3 | 🔴 | Blog không tồn tại | blogId sai | `{"blogId":999999}` | Error |
| 10.1.4 | 🟡 | Xem lại cùng IP | Cùng IP xem lần 2 | (cùng blogId, cùng IP) | `200`, `recorded=false` (có thể) |
| 10.1.5 | 🟡 | Header X-Forwarded-For | Request qua proxy | Header: `X-Forwarded-For: 1.2.3.4` | Ghi nhận IP = 1.2.3.4 |

---

## 11. Notification API

### 11.1 `GET /api/v1/notifications` — Danh sách thông báo

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 11.1.1 | 🟢 | Lấy thông báo thành công | User có thông báo | — | `200`, PageResponse<NotificationResponse> |
| 11.1.2 | 🟢 | Phân trang | size=10 mặc định | `?page=0&size=5` | `200` |
| 11.1.3 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 11.1.4 | 🟡 | Chưa có thông báo | User mới | — | `200`, content = [] |

---

### 11.2 `GET /api/v1/notifications/unread-count` — Số thông báo chưa đọc

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 11.2.1 | 🟢 | Đếm thành công | Có thông báo unread | `200`, data = số lượng (long) |
| 11.2.2 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 11.2.3 | 🟡 | Tất cả đã đọc | Không có unread | `200`, data = 0 |

---

### 11.3 `PUT /api/v1/notifications/{id}/read` — Đánh dấu đã đọc

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 11.3.1 | 🟢 | Đánh dấu thành công | Thông báo chưa đọc | `200` |
| 11.3.2 | 🔴 | Notification không tồn tại | id sai | Error |
| 11.3.3 | 🔒 | Đánh dấu thông báo của người khác | Không phải owner | Error hoặc `403` |
| 11.3.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 11.3.5 | 🟡 | Đánh dấu thông báo đã đọc rồi | Gọi lại lần 2 | `200` (idempotent) |

---

### 11.4 `PUT /api/v1/notifications/read-all` — Đánh dấu tất cả đã đọc

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 11.4.1 | 🟢 | Đánh dấu tất cả thành công | Có nhiều thông báo unread | `200` |
| 11.4.2 | 🔒 | Không có token | Chưa đăng nhập | `401` |
| 11.4.3 | 🟡 | Không có thông báo nào | User mới | `200` (không lỗi) |

---

## 12. Notification Setting API

### 12.1 `GET /api/v1/settings/notifications` — Lấy cài đặt thông báo

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 12.1.1 | 🟢 | Lấy cài đặt thành công | User đã login | `200`, NotificationSettingResponse: `{followers, comments, likes, mentions, newsletter, features}` |
| 12.1.2 | 🔒 | Không có token | Chưa đăng nhập | `401` |

---

### 12.2 `PUT /api/v1/settings/notifications` — Cập nhật cài đặt thông báo

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 12.2.1 | 🟢 | Cập nhật thành công | Tắt/bật tất cả | `{"followers":true,"comments":false,"likes":true,"mentions":false,"newsletter":true,"features":false}` | `200`, trả về setting mới |
| 12.2.2 | 🟢 | Cập nhật một phần | Chỉ gửi một số trường | `{"followers":false}` | `200` |
| 12.2.3 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

## 13. File Upload API

### 13.1 `POST /api/v1/file` — Tạo presigned upload URL

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 13.1.1 | 🟢 | Tạo URL thành công | Thông tin file đầy đủ (gọi file-service) | `{"fileName":"photo.jpg","contentType":"image/jpeg","folder":"blog/temp","fileSize":2048}` | `200`, `{uploadUrl, objectKey (tempKey), formData: {}, publicUrl (URL xem trước bản tạm)}` |
| 13.1.2 | 🔴 | fileName rỗng | fileName = "" | `{"fileName":"","contentType":"image/jpeg","folder":"blog","fileSize":1024}` | `400`, `"Tên file không được để trống"` |
| 13.1.3 | 🔴 | contentType rỗng | contentType = "" | `{...,"contentType":"",...}` | `400`, `"Loại file (contentType) không được để trống"` |
| 13.1.4 | 🔴 | folder rỗng | folder = "" | `{...,"folder":"",...}` | `400`, `"Thư mục không được để trống"` |
| 13.1.5 | 🔴 | fileSize = 0 | Kích thước = 0 | `{...,"fileSize":0}` | `400`, `"Kích thước file phải lớn hơn 0"` |
| 13.1.6 | 🔴 | fileSize < 0 | Kích thước âm | `{...,"fileSize":-1}` | `400` |
| 13.1.7 | 🔴 | fileSize vượt giới hạn | post-image tối đa 5 MB, avatar 2 MB | `{...,"folder":"blog/temp","fileSize":999999999}` | `400`, message của file-service (`... exceeds limit ...`) |
| 13.1.8 | 🔴 | folder không hỗ trợ | Chỉ nhận `blog/temp`, `blog/thumbnails`, `profile/avatar` | `{...,"folder":"other"}` | `400`, `"Thư mục upload không hợp lệ: other"` |
| 13.1.9 | 🔴 | contentType không cho phép | PDF làm ảnh bài viết | `{...,"contentType":"application/pdf","folder":"blog/temp"}` | `400`, message của file-service (`invalid MIME type`) |
| 13.1.10 | 🟡 | file-service không chạy | Không kết nối được `FILE_SERVICE_URL` | body hợp lệ | `503`, `"Dịch vụ lưu trữ file tạm thời không khả dụng"` |
| 13.1.11 | 🟡 | Sai API key | `FILE_SERVICE_API_KEY` không khớp | body hợp lệ | `500`, `"Cấu hình file-service không hợp lệ"` |
| 13.1.12 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |

---

## 14. Statistics API

### 14.1 `GET /api/v1/stats/home` — Thống kê trang chủ

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 14.1.1 | 🟢 | Lấy thống kê thành công | Public, không cần auth | `200`, HomeStatsResponse: `{totalBlogs, totalAuthors, totalLikes, totalCategories}` |
| 14.1.2 | 🟡 | Database trống | Chưa có dữ liệu | `200`, tất cả = 0 |

---

## 15. Report API (User)

### 15.1 `POST /api/v1/reports` — Gửi báo cáo vi phạm

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 15.1.1 | 🟢 | Báo cáo BLOG thành công | Report blog vi phạm | `{"targetType":"BLOG","targetId":1,"reason":"Nội dung spam"}` | `200`, ReportResponse |
| 15.1.2 | 🟢 | Báo cáo COMMENT | Report comment | `{"targetType":"COMMENT","targetId":5,"reason":"Ngôn ngữ thô tục"}` | `200` |
| 15.1.3 | 🟢 | Báo cáo USER | Report user | `{"targetType":"USER","targetId":3,"reason":"Tài khoản giả mạo"}` | `200` |
| 15.1.4 | 🔴 | targetType rỗng | targetType = null | `{"targetType":null,"targetId":1,"reason":"abc"}` | `400`, `"Loại đối tượng báo cáo không được để trống"` |
| 15.1.5 | 🔴 | targetId rỗng | targetId = null | `{"targetType":"BLOG","targetId":null,"reason":"abc"}` | `400`, `"ID đối tượng báo cáo không được để trống"` |
| 15.1.6 | 🔴 | reason rỗng | reason = "" | `{"targetType":"BLOG","targetId":1,"reason":""}` | `400`, `"Lý do báo cáo không được để trống"` |
| 15.1.7 | 🔒 | Không có token | Chưa đăng nhập | — | `401` |
| 15.1.8 | 🔴 | targetType không hợp lệ | Giá trị không trong enum | `{"targetType":"INVALID",...}` | `400` |
| 15.1.9 | 🟡 | Target không tồn tại | targetId sai | `{"targetType":"BLOG","targetId":999999,"reason":"abc"}` | Error |

---

## 16. Admin — Blog Management API

> ⚠️ **Yêu cầu quyền ADMIN**. Tất cả API trong section này cần token của user có role ADMIN.

### 16.1 `GET /api/v1/admin/blogs/{id}` — Xem chi tiết blog (Admin)

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 16.1.1 | 🟢 | Xem thành công | Admin xem blog bất kỳ | `200`, BlogResponse |
| 16.1.2 | 🔴 | Blog không tồn tại | id sai | Error |
| 16.1.3 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |
| 16.1.4 | 🔒 | Không có token | Chưa đăng nhập | `401` |

---

### 16.2 `GET /api/v1/admin/blogs` — Danh sách blog cần duyệt

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 16.2.1 | 🟢 | Lấy danh sách thành công | Admin login | — | `200`, PageResponse<BlogResponse> |
| 16.2.2 | 🟢 | Lọc theo status PENDING | Chỉ bài chờ duyệt | `?status=PENDING` | `200` |
| 16.2.3 | 🟢 | Tìm theo keyword | Tìm theo tiêu đề | `?keyword=Spring` | `200` |
| 16.2.4 | 🟢 | Kết hợp filter | status + keyword | `?status=PENDING&keyword=Java` | `200` |
| 16.2.5 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |

---

### 16.3 `PUT /api/v1/admin/blogs/{id}/approve` — Phê duyệt bài viết

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 16.3.1 | 🟢 | Duyệt thành công | Blog PENDING chuyển sang PUBLISHED | `200`, status = PUBLISHED |
| 16.3.2 | 🔴 | Blog không tồn tại | id sai | Error |
| 16.3.3 | 🔒 | User thường duyệt | Role = USER | `401` hoặc `403` |
| 16.3.4 | 🟡 | Duyệt bài đã PUBLISHED | Duyệt lại | Tùy logic |
| 16.3.5 | 🟡 | Duyệt bài DRAFT | Chưa submit | Tùy logic |

---

### 16.4 `PUT /api/v1/admin/blogs/{id}/reject` — Từ chối bài viết

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 16.4.1 | 🟢 | Từ chối thành công | Có lý do | `{"reason":"Vi phạm quy định nội dung"}` | `200`, status = REJECTED |
| 16.4.2 | 🔴 | Lý do rỗng | reason = "" | `{"reason":""}` | `400`, `"Lý do từ chối không được để trống"` |
| 16.4.3 | 🔴 | Blog không tồn tại | id sai | — | Error |
| 16.4.4 | 🔒 | User thường từ chối | Role = USER | `401` hoặc `403` |

---

### 16.5 `GET /api/v1/admin/blogs/export` — Xuất CSV bài viết

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 16.5.1 | 🟢 | Xuất CSV thành công | Không filter | — | `200`, Content-Type: `text/csv`, header `Content-Disposition: attachment; filename=blogs_export.csv` |
| 16.5.2 | 🟢 | Xuất có filter | status + keyword | `?status=PUBLISHED&keyword=Java` | `200`, file CSV filtered |
| 16.5.3 | 🔒 | User thường xuất | Role = USER | `401` hoặc `403` |
| 16.5.4 | 🟡 | Không có dữ liệu | Database trống | — | `200`, CSV rỗng (chỉ header) |

---

## 17. Admin — User Management API

### 17.1 `GET /api/v1/admin/users` — Danh sách người dùng

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 17.1.1 | 🟢 | Lấy danh sách thành công | Admin login | — | `200`, PageResponse<AdminUserResponse> |
| 17.1.2 | 🟢 | Lọc theo role | role = "ADMIN" | `?role=ADMIN` | `200` |
| 17.1.3 | 🟢 | Lọc theo status | status = ACTIVE | `?status=ACTIVE` | `200` |
| 17.1.4 | 🟢 | Tìm theo keyword | Tìm email | `?keyword=user@test` | `200` |
| 17.1.5 | 🟢 | Sắp xếp | sortBy + sortDir | `?sortBy=email&sortDir=ASC` | `200`, sắp xếp đúng |
| 17.1.6 | 🟢 | Phân trang | page=1, size=5 | `?page=1&size=5` | `200` |
| 17.1.7 | 🟢 | Kết hợp tất cả filter | role + status + keyword + sort | (kết hợp) | `200` |
| 17.1.8 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |

---

### 17.2 `PUT /api/v1/admin/users/{userId}/status` — Cập nhật trạng thái user

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 17.2.1 | 🟢 | Ban user thành công | ACTIVE chuyển sang BANNED | `{"status":"BANNED","reason":"Vi phạm quy định"}` | `200`, AdminUserResponse |
| 17.2.2 | 🟢 | Unban user | BANNED chuyển sang ACTIVE | `{"status":"ACTIVE","reason":"Hết thời hạn ban"}` | `200` |
| 17.2.3 | 🔴 | User không tồn tại | userId sai | — | Error |
| 17.2.4 | 🔒 | User thường cập nhật | Role = USER | `401` hoặc `403` |
| 17.2.5 | 🟡 | Ban chính mình | userId = admin's id | — | Tùy logic |
| 17.2.6 | 🟡 | Status không hợp lệ | status không trong enum | `{"status":"INVALID"}` | `400` |

---

### 17.3 `PUT /api/v1/admin/users/{userId}/role` — Cập nhật vai trò user

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 17.3.1 | 🟢 | Nâng lên ADMIN | USER chuyển sang ADMIN | `{"role":"ADMIN"}` | `200` |
| 17.3.2 | 🟢 | Hạ xuống USER | ADMIN chuyển sang USER | `{"role":"USER"}` | `200` |
| 17.3.3 | 🔴 | User không tồn tại | userId sai | — | Error |
| 17.3.4 | 🔒 | User thường cập nhật | Role = USER | `401` hoặc `403` |
| 17.3.5 | 🟡 | Role không hợp lệ | role = "SUPERADMIN" | `{"role":"SUPERADMIN"}` | Error hoặc bỏ qua |

---

### 17.4 `GET /api/v1/admin/users/export` — Xuất CSV người dùng

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 17.4.1 | 🟢 | Xuất CSV thành công | Không filter | — | `200`, CSV file download |
| 17.4.2 | 🟢 | Xuất có filter | role + status + keyword | `?role=USER&status=ACTIVE` | `200` |
| 17.4.3 | 🔒 | User thường xuất | Role = USER | `401` hoặc `403` |

---

## 18. Admin — Report Management API

### 18.1 `GET /api/v1/admin/reports` — Danh sách báo cáo

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 18.1.1 | 🟢 | Lấy danh sách thành công | Admin login | — | `200`, PageResponse<ReportResponse> |
| 18.1.2 | 🟢 | Lọc theo targetType | targetType = BLOG | `?targetType=BLOG` | `200` |
| 18.1.3 | 🟢 | Lọc theo status | status = PENDING | `?status=PENDING` | `200` |
| 18.1.4 | 🟢 | Kết hợp filter | targetType + status | `?targetType=COMMENT&status=PENDING` | `200` |
| 18.1.5 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |

---

### 18.2 `PUT /api/v1/admin/reports/{id}/resolve` — Xử lý báo cáo

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 18.2.1 | 🟢 | Chấp nhận report | RESOLVED_ACCEPTED | `{"status":"RESOLVED_ACCEPTED","adminNotes":"Nội dung vi phạm, đã xử lý"}` | `200`, ReportResponse |
| 18.2.2 | 🟢 | Từ chối report | RESOLVED_REJECTED | `{"status":"RESOLVED_REJECTED","adminNotes":"Không phát hiện vi phạm"}` | `200` |
| 18.2.3 | 🔴 | status rỗng | status = null | `{"status":null}` | `400`, `"Trạng thái xử lý không được để trống"` |
| 18.2.4 | 🔴 | Report không tồn tại | id sai | — | Error |
| 18.2.5 | 🔒 | User thường xử lý | Role = USER | `401` hoặc `403` |
| 18.2.6 | 🟡 | Xử lý lại report đã resolve | Resolve lần 2 | — | Tùy logic |

---

### 18.3 `POST /api/v1/admin/reports/{id}/penalize` — Xử phạt người dùng

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 18.3.1 | 🟢 | Cảnh cáo user | action = WARN | `{"action":"WARN","reason":"Vi phạm lần 1"}` | `200` |
| 18.3.2 | 🟢 | Khóa tài khoản | action = LOCK | `{"action":"LOCK","reason":"Vi phạm nghiêm trọng"}` | `200` |
| 18.3.3 | 🔴 | action rỗng | action = null | `{"action":null,"reason":"abc"}` | `400`, `"Hình thức xử phạt không được để trống"` |
| 18.3.4 | 🔴 | reason rỗng | reason = "" | `{"action":"WARN","reason":""}` | `400`, `"Lý do xử phạt không được để trống"` |
| 18.3.5 | 🔴 | Report không tồn tại | id sai | — | Error |
| 18.3.6 | 🔒 | User thường xử phạt | Role = USER | `401` hoặc `403` |

---

### 18.4 `GET /api/v1/admin/reports/export` — Xuất CSV báo cáo

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 18.4.1 | 🟢 | Xuất CSV thành công | Không filter | — | `200`, CSV download |
| 18.4.2 | 🟢 | Xuất có filter | targetType + status | `?targetType=BLOG&status=PENDING` | `200` |
| 18.4.3 | 🔒 | User thường xuất | Role = USER | `401` hoặc `403` |

---

## 19. Admin — Dashboard API

### 19.1 `GET /api/v1/admin/dashboard/summary` — Tổng quan số liệu

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 19.1.1 | 🟢 | Lấy summary thành công | Admin login | `200`, AdminDashboardSummaryResponse: `{totalUsers, totalBlogs, totalComments, pendingBlogsCount, pendingReportsCount}` |
| 19.1.2 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |
| 19.1.3 | 🟡 | Database trống | Chưa có dữ liệu | `200`, tất cả = 0 |

---

### 19.2 `GET /api/v1/admin/dashboard/growth` — Dữ liệu tăng trưởng

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 19.2.1 | 🟢 | Lấy dữ liệu 30 ngày (mặc định) | Không gửi days | — | `200`, List<DailyGrowthResponse> gồm `date, newUsersCount, newBlogsCount` |
| 19.2.2 | 🟢 | Lấy dữ liệu 7 ngày | days = 7 | `?days=7` | `200`, 7 entries |
| 19.2.3 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |
| 19.2.4 | 🟡 | Days = 0 | Boundary | `?days=0` | Tùy logic |
| 19.2.5 | 🟡 | Days số âm | days = -1 | `?days=-1` | Tùy logic |
| 19.2.6 | 🟡 | Days rất lớn | days = 365 | `?days=365` | `200` |

---

### 19.3 `GET /api/v1/admin/dashboard/top-blogs` — Bài viết hàng đầu

| # | Loại | Tên test case | Mô tả | Query Params | Expected |
|---|------|---------------|-------|-------------|----------|
| 19.3.1 | 🟢 | Lấy top 10 (mặc định) | Không gửi limit | — | `200`, List<BlogResponse>, tối đa 10 |
| 19.3.2 | 🟢 | Lấy top 5 | limit = 5 | `?limit=5` | `200`, 5 bài viết |
| 19.3.3 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |
| 19.3.4 | 🟡 | Limit = 0 | Boundary | `?limit=0` | Trả rỗng hoặc mặc định |
| 19.3.5 | 🟡 | Không có blog nào | Database trống | — | `200`, data = [] |

---

## 20. Admin — System Settings API

### 20.1 `GET /api/v1/admin/settings` — Lấy cấu hình hệ thống

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 20.1.1 | 🟢 | Lấy cấu hình thành công | Admin login | `200`, `Map<String, String>` |
| 20.1.2 | 🔒 | User thường truy cập | Role = USER | `401` hoặc `403` |

---

### 20.2 `PUT /api/v1/admin/settings` — Cập nhật cấu hình hệ thống

| # | Loại | Tên test case | Mô tả | Request Body | Expected |
|---|------|---------------|-------|-------------|----------|
| 20.2.1 | 🟢 | Cập nhật thành công | Key-value hợp lệ | `{"site_name":"My Blog","max_upload_size":"10MB"}` | `200`, trả settings mới |
| 20.2.2 | 🔒 | User thường cập nhật | Role = USER | — | `401` hoặc `403` |
| 20.2.3 | 🟡 | Body rỗng | settings = {} | `{}` | `200` hoặc không thay đổi |
| 20.2.4 | 🟡 | Key không tồn tại | Key mới | `{"new_key":"value"}` | Tùy logic |

---

## 21. Admin — Seed Data API

### 21.1 `POST /api/v1/admin/seed-data` — Khởi tạo dữ liệu mẫu

| # | Loại | Tên test case | Mô tả | Expected |
|---|------|---------------|-------|----------|
| 21.1.1 | 🟢 | Seed data thành công | Gọi API | `200`, data: `"OK"`, message: `"Khởi tạo dữ liệu mẫu thành công!"` |
| 21.1.2 | 🟡 | Gọi lại lần 2 | Data đã tồn tại | Tùy logic (idempotent hoặc duplicate error) |
| 21.1.3 | 🔒 | User thường gọi | Role = USER | `401` hoặc `403` (tùy config) |

---

## Tổng kết

| Module | Số API endpoints | Số test cases |
|--------|:----------------:|:-------------:|
| Health Check | 1 | 2 |
| Authentication | 11 | 48 |
| Blog | 9 | 44 |
| Category | 3 | 7 |
| Comment | 4 | 20 |
| Like | 2 | 11 |
| Bookmark | 2 | 9 |
| Follow | 2 | 9 |
| Share | 1 | 6 |
| View | 1 | 5 |
| Notification | 4 | 13 |
| Notification Setting | 2 | 5 |
| File Upload | 1 | 7 |
| Statistics | 1 | 2 |
| Report (User) | 1 | 9 |
| Admin — Blog | 5 | 16 |
| Admin — User | 4 | 16 |
| Admin — Report | 4 | 14 |
| Admin — Dashboard | 3 | 12 |
| Admin — System Settings | 2 | 5 |
| Admin — Seed Data | 1 | 3 |
| **Tổng cộng** | **64** | **~263** |

---

## Ghi chú quan trọng

### Cấu trúc Response chung
Tất cả API (trừ Health Check và Export CSV) trả về cấu trúc:
```json
{
  "data": "<T>",
  "message": "string",
  "code": 200
}
```

### Xác thực (Authentication)
- **JWT Cookie-based**: Token được lưu trong cookie `token` (HttpOnly, SameSite=Lax)
- **Public endpoints** (không cần token):
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/register`
  - `GET /api/v1/auth/activeAccount`
  - `POST /api/v1/auth/forgot-password`
  - `POST /api/v1/auth/verify-otp`
  - `POST /api/v1/auth/reset-password`
  - `GET /api/v1/auth/login/social`
  - `GET /api/v1/healthcheck`
  - `GET /api/v1/blogs` và `GET /api/v1/blogs/**`
  - `GET /api/v1/stats/**`
  - `GET /api/v1/comments/blog/**`
  - `GET /api/v1/categories` và `GET /api/v1/categories/**`

### Enum Values
| Enum | Values |
|------|--------|
| `BlogStatus` | `DRAFT`, `PENDING`, `PUBLISHED`, `REJECTED`, `ARCHIVED`, `DELETED` |
| `UserStatus` | `ACTIVE`, `INACTIVE`, `BANNED`, `PENDING` |
| `ReportTargetType` | `BLOG`, `COMMENT`, `USER` |
| `ReportStatus` | `PENDING`, `RESOLVED_ACCEPTED`, `RESOLVED_REJECTED` |
| `PenaltyAction` | `WARN`, `LOCK` |

### Error Handling
Các exception domain trả về cấu trúc:
```json
{
  "data": null,
  "message": "Error message",
  "code": "<HTTP status code>"
}
```
