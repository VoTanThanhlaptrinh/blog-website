# Tài liệu Nghiệp vụ — Nền tảng Mạng xã hội / Blog

> Tổng hợp các nghiệp vụ của hệ thống dựa trên mã nguồn hiện tại.
> Backend: Spring Boot (kiến trúc module hóa theo domain). Frontend: Angular (standalone components, lazy routes).
> Trạng thái: một số nghiệp vụ đã hoàn thiện, một số mới có khung entity/route (đánh dấu bên dưới).

---

## 1. Tổng quan kiến trúc

Backend chia theo các module domain, mỗi module có 4 lớp `api` → `application` → `domain` → `infrastructure`:

| Module | Trách nhiệm nghiệp vụ | Trạng thái |
|---|---|---|
| **identity** | Đăng ký, đăng nhập, hồ sơ, phân quyền, OAuth2 | Đã triển khai |
| **content** | Quản lý bài viết (blog/post) | Đã triển khai |
| **interaction** | Like, Comment, Share, Bookmark, View | Có entity, chưa có API |
| **storage** | Upload ảnh (Cloudflare R2), presigned URL | Đã triển khai |
| **notification** | Gửi email (kích hoạt, OTP) qua event | Đã triển khai |
| **recommendation** | Gợi ý nội dung | Chỉ có khung thư mục (chưa code) |
| **admin** | Quản trị hệ thống | Chỉ có khung thư mục (chưa code) |

Giao tiếp giữa các module dùng **Application Event** (Spring `ApplicationEventPublisher`) để tránh phụ thuộc chéo — ví dụ content phát `BlogImagesActivatedEvent`, storage lắng nghe và xử lý.

---

## 2. Nghiệp vụ Tài khoản & Xác thực (Identity)

Endpoint gốc: `/api/v1/auth`

| Nghiệp vụ | Endpoint | Mô tả |
|---|---|---|
| Đăng ký | `POST /register` | Tạo tài khoản mới (chưa kích hoạt, `enabled=false`). Phát event gửi email kích hoạt. |
| Kích hoạt tài khoản | `GET /activeAccount?token=` | Xác nhận email qua token, chuyển `enabled=true`. |
| Đăng nhập | `POST /login` | Xác thực email/mật khẩu, trả JWT access token + set refresh token (cookie). |
| Đăng nhập mạng xã hội | `GET /login/social` | Trả URL OAuth2 cho Google & Facebook. |
| Làm mới token | `POST /refresh` | Dùng refresh token trong cookie để cấp access token mới. |
| Đăng xuất | `POST /logout` | Xóa cookie phiên. |
| Xem hồ sơ | `GET /profile` | Lấy thông tin người dùng hiện tại. |
| Cập nhật hồ sơ | `PUT /profile` | Sửa bio, ngày sinh, số điện thoại... |
| Lấy URL upload avatar | `POST /profile/avatar/upload-url` | Phát event storage tạo presigned URL cho ảnh đại diện. |
| Quên mật khẩu | `POST /forgot-password` | Gửi mã OTP qua email. |
| Xác thực OTP | `POST /verify-otp` | Kiểm tra OTP, trả token reset. |
| Đặt lại mật khẩu | `POST /reset-password` | Đặt mật khẩu mới bằng token reset. |
| Đổi mật khẩu | `POST /change-password` | Đổi mật khẩu khi đã đăng nhập (cần mật khẩu cũ). |

**Phân quyền (RBAC):** `User` ↔ `UserRole` ↔ `Role`. Người dùng có nhiều vai trò, mỗi gán vai trò có trạng thái (`UserRoleStatus`); chỉ vai trò `ACTIVE` mới được tính là quyền.

**Trạng thái người dùng** (`UserStatus`): `ACTIVE`, `INACTIVE`, `BANNED`, `PENDING`.

Bảo mật: JWT (Spring Security Resource Server), OAuth2 login với custom `OAuth2UserService` và success handler.

---

## 3. Nghiệp vụ Nội dung / Bài viết (Content)

Endpoint gốc: `/api/v1/blogs`

| Nghiệp vụ | Endpoint | Mô tả |
|---|---|---|
| Tạo bài viết | `POST /blogs` | Người dùng đăng bài mới (title, description, content, status). |
| Xem chi tiết | `GET /blogs/{id}` | Lấy 1 bài viết kèm tác giả, số like, số comment. |
| Danh sách / tìm kiếm | `GET /blogs` | Phân trang + lọc theo `keyword`, `status`, `userId`. Sắp xếp mặc định theo ngày tạo giảm dần. |
| Cập nhật | `PUT /blogs/{id}` | Sửa bài (kiểm tra quyền sở hữu). |
| Xóa | `DELETE /blogs/{id}` | Xóa mềm bài viết. |
| Kích hoạt ảnh trong bài | `POST /blogs/images/activate` | Chuyển ảnh từ prefix `temp/` sang `blog/` (phát event cho storage). |

**Trạng thái bài viết** (`BlogStatus`): `DRAFT` (nháp), `PUBLISHED` (công khai), `ARCHIVED` (lưu trữ), `DELETED` (đã xóa).

Ràng buộc dữ liệu (validation): tiêu đề ≤ 255 ký tự và bắt buộc, mô tả ≤ 1000 ký tự và bắt buộc, nội dung bắt buộc.

Quan hệ: `Blog` thuộc về 1 `User`, có nhiều `Comment` và `Like`.

Lỗi nghiệp vụ đã định nghĩa: bài không tồn tại, bài đã bị xóa, truy cập trái phép (không phải chủ sở hữu).

---

## 4. Nghiệp vụ Tương tác (Interaction)

> Đã có entity + repository, **chưa có Controller/Service** (chưa expose API). Đây là các nghiệp vụ đã thiết kế cấu trúc dữ liệu, chờ triển khai.

| Nghiệp vụ | Entity | Mô tả dữ liệu |
|---|---|---|
| **Like** (thích) | `Like` | Cờ `liked`, liên kết `Blog` + `User`. Một người like một bài. |
| **Comment** (bình luận) | `Comment` | Nội dung, người tạo, bài viết, hỗ trợ **trả lời lồng nhau** (`reply` là danh sách comment con). |
| **Share** (chia sẻ) | `Share` | Trạng thái (`ACTIVE`/`DELETED`), `provider` (nền tảng chia sẻ), liên kết `Blog` + `User`. |
| **Bookmark** (lưu bài) | `Bookmark` | Trạng thái (`ACTIVE`/`REMOVED`), liên kết `User` + `Blog`. |
| **View** (lượt xem) | `View` | Hiện chỉ có `id` — cấu trúc chưa hoàn chỉnh (cần bổ sung liên kết Blog/User/thời điểm). |

Nghiệp vụ dự kiến cần bổ sung API:
- Like / Unlike một bài viết.
- Thêm / sửa / xóa bình luận; trả lời bình luận.
- Chia sẻ bài viết ra nền tảng ngoài, đếm lượt share.
- Thêm / gỡ bookmark; xem danh sách bài đã lưu.
- Ghi nhận và đếm lượt xem.

---

## 5. Nghiệp vụ Lưu trữ ảnh (Storage)

Endpoint gốc: `/api/v1/file`. Nhà cung cấp: **Cloudflare R2** (tương thích S3, dùng presigned URL).

| Nghiệp vụ | Cơ chế | Mô tả |
|---|---|---|
| Tạo URL upload | `POST /file` | Trả presigned POST URL để client upload trực tiếp lên R2. |
| Kích hoạt ảnh | qua event/service | Ảnh upload vào `temp/`, khi bài viết/hồ sơ được lưu thì chuyển sang prefix chính thức (`blog/`, avatar...). |
| Đổi prefix hàng loạt | `updateImagePrefixes(...)` | Di chuyển nhiều ảnh giữa các prefix. |

**Trạng thái ảnh** (`ImageStatus`): `ACTIVE`, `INACTIVE`, `DELETED`. Ảnh chưa gắn vào nội dung nào ở `temp/` được coi là tạm và có thể dọn dẹp.

Listeners: `ContentStorageEventListener` (ảnh bài viết), `IdentityUploadEventListener` (ảnh avatar).

---

## 6. Nghiệp vụ Thông báo (Notification)

Cơ chế: lắng nghe **Application Event** bất đồng bộ (`@Async @EventListener`) rồi gửi email.

| Sự kiện | Listener | Email gửi đi |
|---|---|---|
| Đăng ký tài khoản | `MailNotificationListener` | Email kích hoạt tài khoản (kèm token). |
| Quên mật khẩu | `ForgotPasswordEventListener` | Email chứa mã OTP. |

`ApiResponse<T>` (record `data`, `message`, `code`) là định dạng phản hồi thống nhất toàn hệ thống. `GlobalExceptionHandler` xử lý lỗi tập trung, `DomainException` + `ErrorCode` chuẩn hóa lỗi nghiệp vụ.

---

## 7. Giao diện Frontend (Angular)

Route chính (lazy-loaded):

| Route | Nghiệp vụ giao diện |
|---|---|
| `/` | Trang chủ (feed bài viết). |
| `/login`, `/register` | Đăng nhập, đăng ký. |
| `/auth/forgot-password`, `/auth/change-password` | Quên & đổi mật khẩu. |
| `/blog/creation` | Soạn & đăng bài (split editor). |
| `/blog/detail` | Xem chi tiết bài viết. |
| `/profile/split`, `/profile/filters`, `/profile/update` | Trang cá nhân, lọc bài, cập nhật hồ sơ. |
| `/search` | Tìm kiếm theo danh mục (grid). |
| `/settings/notifications` | Cài đặt thông báo. |
| `/admin/*` | Khu quản trị: analytics, quản lý nội dung, báo cáo, cài đặt hệ thống, quản lý người dùng. |

Hạ tầng FE: `jwt.interceptor` gắn token vào request, `auth.service` / `token.service` quản lý phiên, `file.service` xử lý upload.

---

## 8. Nghiệp vụ chưa triển khai / cần hoàn thiện

- **Interaction API**: Like, Comment, Share, Bookmark, View — mới có entity, chưa có controller/service.
- **Recommendation**: gợi ý bài viết — thư mục rỗng.
- **Admin backend**: các trang quản trị FE đã có nhưng API backend chưa được viết.
- **View entity**: thiếu trường liên kết và timestamp.
- **Follow/Following** (theo dõi người dùng): chưa có trong hệ thống — cần bổ sung nếu muốn đầy đủ tính năng mạng xã hội.
- **Feed cá nhân hóa / Notification realtime**: chưa có.

---

## 9. Luồng nghiệp vụ tiêu biểu

**Đăng bài kèm ảnh:**
1. Client xin presigned URL (`POST /api/v1/file`) → upload ảnh vào `temp/`.
2. Client tạo bài (`POST /api/v1/blogs`) với nội dung tham chiếu ảnh temp.
3. Client gọi `POST /api/v1/blogs/images/activate` → content phát `BlogImagesActivatedEvent` → storage chuyển ảnh `temp/` → `blog/`.

**Quên mật khẩu:**
1. `POST /forgot-password` → phát `ForgotPasswordEvent` → gửi OTP email.
2. `POST /verify-otp` → nhận token reset.
3. `POST /reset-password` → đặt mật khẩu mới.
