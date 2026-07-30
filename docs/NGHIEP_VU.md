# Tài liệu Nghiệp vụ — Nền tảng Mạng xã hội / Blog

> Tổng hợp các nghiệp vụ của hệ thống dựa trên mã nguồn hiện tại (cập nhật mới nhất).
> Backend: Spring Boot (kiến trúc module hóa theo domain). Frontend: Angular (standalone components, lazy routes).
> Trạng thái: Cập nhật chi tiết tiến độ triển khai Backend API và Frontend UI bên dưới.

---

## 1. Tổng quan kiến trúc

Backend chia theo các module domain, mỗi module có 4 lớp `api` → `application` → `domain` → `infrastructure`:

| Module | Trách nhiệm nghiệp vụ | Trạng thái Backend | Trạng thái Frontend |
|---|---|---|---|
| **identity** | Đăng ký, đăng nhập, hồ sơ, phân quyền, OAuth2 | Đã triển khai hoàn thiện | Đã tích hợp `AuthService` & `TokenService` |
| **content** | Quản lý bài viết (blog/post), Tiền kiểm bài viết | Đã triển khai hoàn thiện (Hỗ trợ `DRAFT`, `PENDING`, `PUBLISHED`, `REJECTED`) | Dùng dữ liệu mẫu (chưa tích hợp API) |
| **interaction** | Like, Comment, Share, Bookmark, View | Đã triển khai đầy đủ API & Service | Dùng dữ liệu mẫu (chưa tích hợp API) |
| **storage** | Upload ảnh (Cloudflare R2), presigned URL | Đã triển khai | Đã tích hợp `FileService` |
| **notification** | Gửi email (kích hoạt, OTP) & In-App Notification (Moderation events) | Đã triển khai Email & In-App Notification | Chưa có giao diện xem thông báo thật |
| **recommendation** | Gợi ý nội dung | Chưa code (chỉ có khung thư mục) | Dùng dữ liệu mẫu |
| **admin** | Quản trị hệ thống (Duyệt bài, Báo cáo, Dashboard Analytics) | Đã triển khai đầy đủ API & Service | Đã có UI mẫu (chưa kết nối API) |

Giao tiếp giữa các module dùng **Application Event** (Spring `ApplicationEventPublisher`) để tránh phụ thuộc chéo — ví dụ content/admin phát `BlogModerationEvent`, notification lắng nghe tạo In-App Notification & gửi Email.

---

## 2. Nghiệp vụ Tài khoản & Xác thực (Identity)

Endpoint gốc: `/api/v1/auth`

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái |
|---|---|---|---|
| Đăng ký | `POST /register` | Tạo tài khoản mới (chưa kích hoạt, `enabled=false`). Phát event gửi email kích hoạt. | Đã triển khai |
| Kích hoạt tài khoản | `GET /activeAccount?token=` | Xác nhận email qua token, chuyển `enabled=true`. | Đã triển khai |
| Đăng nhập | `POST /login` | Xác thực email/mật khẩu, trả JWT access token + set refresh token (cookie). | Đã triển khai |
| Đăng nhập mạng xã hội | `GET /login/social` | Trả URL OAuth2 cho Google & Facebook. | Đã triển khai |
| Làm mới token | `POST /refresh` | Dùng refresh token trong cookie để cấp access token mới. | Đã triển khai |
| Đăng xuất | `POST /logout` | Xóa cookie phiên. | Đã triển khai |
| Xem hồ sơ | `GET /profile` | Lấy thông tin người dùng hiện tại. | Đã triển khai |
| Cập nhật hồ sơ | `PUT /profile` | Sửa bio, ngày sinh, số điện thoại... | Đã triển khai |
| Lấy URL upload avatar | `POST /profile/avatar/upload-url` | Phát event storage tạo presigned URL cho ảnh đại diện. | Đã triển khai |
| Quên mật khẩu | `POST /forgot-password` | Gửi mã OTP qua email. | Đã triển khai |
| Xác thực OTP | `POST /verify-otp` | Kiểm tra OTP, trả token reset. | Đã triển khai |
| Đặt lại mật khẩu | `POST /reset-password` | Đặt mật khẩu mới bằng token reset. | Đã triển khai |
| Đổi mật khẩu | `POST /change-password` | Đổi mật khẩu khi đã đăng nhập (cần mật khẩu cũ). | Đã triển khai |

**Phân quyền (RBAC):** `User` ↔ `UserRole` ↔ `Role`. Người dùng có nhiều vai trò, mỗi gán vai trò có trạng thái (`UserRoleStatus`); chỉ vai trò `ACTIVE` mới được tính là quyền.

**Trạng thái người dùng** (`UserStatus`): `ACTIVE`, `INACTIVE`, `BANNED`, `PENDING`.

Bảo mật: JWT (Spring Security Resource Server), OAuth2 login với custom `OAuth2UserService` và success handler.

---

## 3. Nghiệp vụ Nội dung / Bài viết (Content)

Endpoint gốc: `/api/v1/blogs`

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái |
|---|---|---|---|
| Tạo bài viết | `POST /blogs` | Người dùng đăng bài mới. Áp dụng luật Tiền kiểm (mặc định chuyển sang `PENDING` chờ duyệt, trừ khi là `DRAFT`). | Đã triển khai Backend |
| Xem chi tiết | `GET /blogs/{id}` | Lấy 1 bài viết kèm tác giả, số like, số comment, số view, số share. | Đã triển khai Backend |
| Danh sách / tìm kiếm | `GET /blogs` | Phân trang + lọc theo `keyword`, `status`, `userId`. Sắp xếp mặc định theo ngày tạo giảm dần. | Đã triển khai Backend |
| Cập nhật | `PUT /blogs/{id}` | Sửa bài (kiểm tra quyền sở hữu). | Đã triển khai Backend |
| Xóa | `DELETE /blogs/{id}` | Xóa mềm bài viết. | Đã triển khai Backend |
| Kích hoạt ảnh trong bài | `POST /blogs/images/activate` | Chuyển ảnh từ prefix `temp/` sang `blog/` (phát event cho storage). | Đã triển khai Backend |

**Trạng thái bài viết** (`BlogStatus`): `DRAFT` (nháp), `PENDING` (chờ duyệt), `PUBLISHED` (công khai), `REJECTED` (từ chối), `ARCHIVED` (lưu trữ), `DELETED` (đã xóa).

Ràng buộc dữ liệu (validation): tiêu đề ≤ 255 ký tự và bắt buộc, mô tả ≤ 1000 ký tự và bắt buộc, nội dung bắt buộc.

Quan hệ: `Blog` thuộc về 1 `User`, có nhiều `Comment`, `Like`, `Bookmark`, `Share`, `View`.

Lỗi nghiệp vụ đã định nghĩa: bài không tồn tại, bài đã bị xóa, truy cập trái phép (không phải chủ sở hữu).

---

## 4. Nghiệp vụ Quản trị hệ thống (Admin)

Endpoint gốc: `/api/v1/admin` (yêu cầu quyền `ADMIN`)

### A. Kiểm duyệt nội dung (Moderation)

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái Backend |
|---|---|---|---|
| Danh sách duyệt bài | `GET /api/v1/admin/blogs` | Lấy danh sách bài viết theo trạng thái (`PENDING`, `REJECTED`, etc.) để Admin kiểm duyệt. | Đã triển khai |
| Phê duyệt bài viết | `PUT /api/v1/admin/blogs/{id}/approve` | Đổi trạng thái bài viết thành `PUBLISHED`, phát `BlogModerationEvent` gửi thông báo cho tác giả. | Đã triển khai |
| Từ chối bài viết | `PUT /api/v1/admin/blogs/{id}/reject` | Nhận `reason` từ chối, đổi trạng thái bài viết thành `REJECTED`, phát `BlogModerationEvent` gửi thông báo cho tác giả. | Đã triển khai |

### B. Quản lý báo cáo vi phạm (Reports)

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái Backend |
|---|---|---|---|
| Gửi báo cáo (User) | `POST /api/v1/reports` | Người dùng báo cáo bài viết (`BLOG`), bình luận (`COMMENT`), hoặc người dùng (`USER`). | Đã triển khai |
| Danh sách báo cáo (Admin) | `GET /api/v1/admin/reports` | Admin xem danh sách báo cáo vi phạm (lọc theo `targetType`, `status`). | Đã triển khai |
| Giải quyết báo cáo (Admin) | `PUT /api/v1/admin/reports/{id}/resolve` | Đánh dấu trạng thái xử lý (`RESOLVED_ACCEPTED` / `RESOLVED_REJECTED`) kèm ghi chú Admin (`adminNotes`). | Đã triển khai |

### C. Thống kê & Analytics (Dashboard)

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái Backend |
|---|---|---|---|
| Số liệu tổng quan | `GET /api/v1/admin/dashboard/summary` | Trả về tổng số User, Blog (PUBLISHED), Comment, số bài viết chờ duyệt (`pendingBlogsCount`), số báo cáo chờ xử lý (`pendingReportsCount`). | Đã triển khai |
| Biểu đồ tăng trưởng | `GET /api/v1/admin/dashboard/growth` | Trả về số lượng User mới và Blog mới theo từng ngày (mặc định 30 ngày). | Đã triển khai |
| Top bài viết nổi bật | `GET /api/v1/admin/dashboard/top-blogs` | Trả về danh sách bài viết có số lượt xem cao nhất. | Đã triển khai |

---

## 5. Nghiệp vụ Tương tác (Interaction)

> Đã triển khai đầy đủ Controller/Service/DTO/Repository Backend cho tất cả các nghiệp vụ **Like**, **Comment**, **Bookmark**, **Share** và **View**.

| Nghiệp vụ | Endpoint | Mô tả | Trạng thái Backend |
|---|---|---|---|
| Toggle Like bài viết | `POST /api/v1/likes/toggle` | Thích / bỏ thích bài viết. Trả về tổng số like + trạng thái `isLiked`. | Đã triển khai |
| Toggle Like bình luận | `POST /api/v1/likes/comment/toggle` | Thích / bỏ thích bình luận (`CommentLike`). Trả về số like + `isLiked`. | Đã triển khai |
| Tạo bình luận | `POST /api/v1/comments` | Bình luận bài viết hoặc trả lời comment khác (`parentId`, tối đa 1 cấp reply). | Đã triển khai |
| Cập nhật bình luận | `PUT /api/v1/comments/{id}` | Chỉnh sửa nội dung bình luận (kiểm tra quyền chủ sở hữu). | Đã triển khai |
| Xóa bình luận | `DELETE /api/v1/comments/{id}` | Xóa mềm bình luận (chuyển trạng thái sang `DELETED`). | Đã triển khai |
| Danh sách bình luận theo Blog | `GET /api/v1/comments/blog/{blogId}` | Phân trang danh sách bình luận kèm các câu trả lời con (replies). | Đã triển khai |
| Toggle Bookmark | `POST /api/v1/bookmarks/toggle` | Thêm / gỡ lưu bài viết. Trả về trạng thái `bookmarked`. | Đã triển khai |
| Danh sách Bookmark của tôi | `GET /api/v1/bookmarks/me` | Phân trang danh sách các bài viết người dùng đã lưu. | Đã triển khai |
| Ghi nhận Share | `POST /api/v1/shares` | Ghi nhận lượt chia sẻ ra các nền tảng (`provider`) và cộng dồn lượt share trên bài viết. | Đã triển khai |
| Ghi nhận View | `POST /api/v1/views/record` | Ghi nhận lượt xem bài viết kèm cơ chế anti-spam (giới hạn 1 view / User hoặc IP trong 24h). | Đã triển khai |

---

## 6. Nghiệp vụ Lưu trữ ảnh (Storage)

Endpoint gốc: `/api/v1/file`. Nhà cung cấp: **Cloudflare R2** (tương thích S3, dùng presigned URL).

| Nghiệp vụ | Cơ chế | Mô tả |
|---|---|---|
| Tạo URL upload | `POST /file` | Trả presigned POST URL để client upload trực tiếp lên R2. |
| Kích hoạt ảnh | qua event/service | Ảnh upload vào `temp/`, khi bài viết/hồ sơ được lưu thì chuyển sang prefix chính thức (`blog/`, avatar...). |
| Đổi prefix hàng loạt | `updateImagePrefixes(...)` | Di chuyển nhiều ảnh giữa các prefix. |

Listeners: `ContentStorageEventListener` (ảnh bài viết), `IdentityUploadEventListener` (ảnh avatar).

---

## 7. Nghiệp vụ Thông báo (Notification)

Endpoint gốc: `/api/v1/notifications`

Cơ chế: Lắng nghe **Application Event** bất đồng bộ (`@Async @EventListener`).

| Nghiệp vụ / Sự kiện | Endpoint / Listener | Mô tả |
|---|---|---|
| Thông báo In-App | `GET /api/v1/notifications` | Lấy danh sách thông báo của người dùng đăng nhập. |
| Số lượng chưa đọc | `GET /api/v1/notifications/unread-count` | Lấy số lượng thông báo chưa đọc. |
| Đánh dấu đã đọc | `PUT /api/v1/notifications/{id}/read` | Đánh dấu 1 thông báo là đã đọc. |
| Sự kiện Duyệt bài | `BlogModerationEventListener` | Lắng nghe `BlogModerationEvent`, tạo thông báo In-App + gửi Email thông báo bài được Duyệt/bị Từ chối. |
| Sự kiện Đăng ký | `MailNotificationListener` | Gửi Email kích hoạt tài khoản. |
| Sự kiện Quên mật khẩu | `ForgotPasswordEventListener` | Gửi Email mã OTP. |

---

## 8. Giao diện Frontend (Angular)

Route chính (lazy-loaded):

| Route | Nghiệp vụ giao diện | Trạng thái tích hợp API |
|---|---|---|
| `/` | Trang chủ (feed bài viết). | Đã có UI mẫu (đang dùng mock data) |
| `/login`, `/register` | Đăng nhập, đăng ký. | Đã kết nối API (`AuthService`) |
| `/auth/forgot-password`, `/auth/change-password` | Quên & đổi mật khẩu. | Đã kết nối API (`AuthService`) |
| `/blog/creation` | Soạn & đăng bài (split editor). | Đã có UI mẫu & upload ảnh (chưa gọi API tạo bài) |
| `/blog/detail` | Xem chi tiết bài viết. | Đã có UI mẫu (đang dùng mock data) |
| `/profile/split`, `/profile/filters`, `/profile/update` | Trang cá nhân, lọc bài, cập nhật hồ sơ. | Đã có UI & tích hợp avatar (còn lại mock data) |
| `/search` | Tìm kiếm theo danh mục (grid). | Đã có UI mẫu (chưa gọi API tìm kiếm) |
| `/settings/notifications` | Cài đặt thông báo. | Đã có UI tĩnh |
| `/admin/*` | Khu quản trị: analytics, quản lý nội dung, báo cáo, cài đặt hệ thống, quản lý người dùng. | Đã có UI mẫu (chưa gọi API backend) |

---

## 9. Nghiệp vụ chưa triển khai / cần hoàn thiện

### A. Backend

1. **Recommendation Module**: Chưa viết code (thư mục `recommendation` rỗng). Cần xây dựng thuật toán gợi ý bài viết liên quan, bài viết nổi bật (trending), và feed cá nhân hóa.
2. **Realtime Notification**: WebSocket / SSE để đẩy thông báo In-App ngay lập tức không cần F5 page.
3. **Follow/Following System (Theo dõi tác giả)**: Chưa có Entity `Follow`, API theo dõi/bỏ theo dõi, số lượng follower/following và feed bài viết từ người theo dõi.

### B. Frontend

1. **Tích hợp Interaction API & Content API**:
   - Viết `BlogService` để kết nối API đăng bài, lấy danh sách bài viết, xem chi tiết bài viết.
   - Viết `InteractionService` để kết nối API Like, Comment, Bookmark, Share, View thật trên giao diện bài viết (`blog-detail`) và trang chủ (`home`).
2. **Tích hợp Admin API**: Kết nối các trang UI trong `/admin/*` với các API backend vừa xây dựng (`AdminBlogController`, `AdminReportController`, `AdminDashboardController`).
3. **Tích hợp Notification API**: Kết nối giao diện thông báo với `/api/v1/notifications`.

---

## 10. Luồng nghiệp vụ tiêu biểu

**Đăng bài & Duyệt bài (Proactive Moderation):**
1. Tác giả tạo bài (`POST /api/v1/blogs`) → bài viết ở trạng thái `PENDING`.
2. Admin xem danh sách bài chờ duyệt (`GET /api/v1/admin/blogs?status=PENDING`).
3. Admin bấm Duyệt (`PUT /api/v1/admin/blogs/{id}/approve`) hoặc Từ chối (`PUT /api/v1/admin/blogs/{id}/reject`).
4. Hệ thống phát `BlogModerationEvent` → `BlogModerationEventListener` tự động tạo In-App Notification và gửi Email thông báo kết quả cho Tác giả.
