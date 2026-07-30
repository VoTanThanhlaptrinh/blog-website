-- ==============================================================================
-- DỮ LIỆU MẪU (MOCK DATA) CHO HỆ THỐNG BLOG / MẠNG XÃ HỘI
-- Sử dụng để import trực tiếp vào PostgreSQL (DBeaver, PGAdmin, psql, Neon DB)
-- ==============================================================================

-- 1. ROLES
INSERT INTO roles (id, name, status, created_date, modified_date) VALUES
(1, 'ROLE_ADMIN', 'ACTIVE', NOW(), NOW()),
(2, 'ROLE_USER', 'ACTIVE', NOW(), NOW()),
(3, 'ROLE_MODERATOR', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. IMAGES (AVATARS & COVERS)
INSERT INTO images (id, url, description, content_type, status, created_date, modified_date) VALUES
(1, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', 'Avatar Admin', 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(2, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', 'Avatar John Doe', 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(3, 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', 'Avatar Alice Smith', 'image/jpeg', 'ACTIVE', NOW(), NOW()),
(4, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', 'Avatar Tech Lead', 'image/jpeg', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 3. USERS (Mật khẩu mặc định của tất cả user: Password123!)
-- Hash BCrypt của Password123!: $2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO
INSERT INTO users (id, email, password, phone, bio, birth_date, avatar_id, enabled, status, created_date, modified_date) VALUES
(1, 'admin@blog.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0988888888', 'Quản trị viên hệ thống Blog Platform.', '1990-01-01', 1, true, 'ACTIVE', NOW(), NOW()),
(2, 'mod@blog.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0977777777', 'Kểm duyệt viên nội dung bài viết.', '1992-05-15', NULL, true, 'ACTIVE', NOW(), NOW()),
(3, 'john.doe@techblog.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0912345678', 'Senior Fullstack Developer. Yêu thích Spring Boot & Angular.', '1995-03-20', 2, true, 'ACTIVE', NOW(), NOW()),
(4, 'alice.smith@dev.io', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0923456789', 'Frontend Specialist & UI/UX enthusiast.', '1997-08-10', 3, true, 'ACTIVE', NOW(), NOW()),
(5, 'tech.lead@company.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0934567890', 'Solution Architect với 10 năm kinh nghiệm.', '1988-11-25', 4, true, 'ACTIVE', NOW(), NOW()),
(6, 'pending.user@test.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0945678901', 'Tài khoản chưa kích hoạt email.', NULL, false, 'PENDING', NOW(), NOW()),
(7, 'banned.user@test.com', '$2a$10$7EqJtq986Pay23L8a5B.N.oR15nO6i9fTq5v.Kz0rL8rN6a2mJ0KO', '0956789012', 'Tài khoản vi phạm quy định cộng đồng.', NULL, true, 'BANNED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 4. USER_ROLES
INSERT INTO user_roles (id, user_id, role_id, status, created_date, modified_date) VALUES
(1, 1, 1, 'ACTIVE', NOW(), NOW()),
(2, 2, 3, 'ACTIVE', NOW(), NOW()),
(3, 3, 2, 'ACTIVE', NOW(), NOW()),
(4, 4, 2, 'ACTIVE', NOW(), NOW()),
(5, 5, 2, 'ACTIVE', NOW(), NOW()),
(6, 6, 2, 'ACTIVE', NOW(), NOW()),
(7, 7, 2, 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 5. BLOGS
INSERT INTO blogs (id, title, description, content, status, rejection_reason, view_count, share_count, user_id, created_date, modified_date) VALUES
(1, 'Hướng dẫn lập trình Java Spring Boot 3 từ A đến Z', 'Tìm hiểu toàn bộ tính năng mới trong Spring Boot 3, Spring Security 6 và AOT compilation.', '<p>Spring Boot 3 mang đến nhiều cải tiến mạnh mẽ vượt trội với Java 17+, GraalVM Native Image và Spring Security 6...</p>', 'PUBLISHED', NULL, 1250, 45, 3, NOW(), NOW()),
(2, 'Xây dựng ứng dụng Angular với Standalone Components & Signals', 'Khám phá mô hình kiến trúc mới nhất của Angular giúp tối ưu hiệu năng và giản lược cấu hình NgModule.', '<p>Angular v16+ giới thiệu Signals và Standalone Components làm thay đổi hoàn toàn cách quản lý State...</p>', 'PUBLISHED', NULL, 980, 32, 4, NOW(), NOW()),
(3, 'Tối ưu hóa hiệu năng Database PostgreSQL trong các ứng dụng tải cao', 'Chiến lược đánh Index, Partitioning, Connection Pooling với HikariCP và xử lý N+1 Query trong JPA.', '<p>Bài viết chia sẻ các kỹ thuật thực chiến giúp PostgreSQL đạt 10.000 QPS mà không bị tắc nghẽn I/O...</p>', 'PUBLISHED', NULL, 1560, 88, 5, NOW(), NOW()),
(4, 'Thiết kế hệ thống Microservices & RESTful API chuẩn REST Client', 'Nguyên tắc thiết kế API rõ ràng, phiên bản hóa API, xử lý lỗi toàn cục và OpenAPI documentation.', '<p>Một RESTful API chất lượng cần đáp ứng tính đồng nhất trong mã lỗi, chuẩn hóa response format...</p>', 'PUBLISHED', NULL, 740, 19, 3, NOW(), NOW()),
(5, 'Tích hợp lưu trữ hình ảnh với Cloudflare R2 Storage & Presigned URL', 'Giải pháp lưu trữ media tối ưu chi phí, không tốn băng thông egress và bảo mật bằng presigned request.', '<p>Sử dụng S3 API client kết nối Cloudflare R2 để tải ảnh trực tiếp từ trình duyệt giúp giảm tải server backend...</p>', 'PUBLISHED', NULL, 620, 15, 4, NOW(), NOW()),
(6, 'Bảo mật ứng dụng Web với OAuth2, JWT và Spring Security', 'Phân tích cơ chế mã hóa JWT Stateless, Token Refresh Cookie và tích hợp Google/Facebook Login.', '<p>Định danh người dùng qua OAuth2 và Resource Server JWT giúp hệ thống mở rộng linh hoạt theo kiến trúc Microservices...</p>', 'PUBLISHED', NULL, 2100, 110, 5, NOW(), NOW()),
(7, 'Đánh giá các mô hình AI mã nguồn mở năm 2026', 'Phân tích năng lực của Llama 4, DeepSeek R1 và các mô hình LLM chạy local.', '<p>Các mô hình AI mã nguồn mở đang tiến rất gần đến trình độ của các mô hình thương mại hàng đầu...</p>', 'PENDING', NULL, 0, 0, 4, NOW(), NOW()),
(8, 'Kinh nghiệm chuyển dịch hệ thống từ Monolith sang Microservices', 'Bài học kinh nghiệm thực tế về phân tách database, saga pattern và distributed tracing.', '<p>Chuyển đổi sang Microservices không chỉ là thay đổi công nghệ mà là thay đổi cấu trúc tổ chức team...</p>', 'PENDING', NULL, 0, 0, 3, NOW(), NOW()),
(9, 'Quảng cáo dịch vụ tăng tương tác không rõ nguồn gốc', 'Cung cấp dịch vụ tăng follow và like tự động giá rẻ.', '<p>Truy cập ngay trang web xyz để nhận ưu đãi mua view giá rẻ...</p>', 'REJECTED', 'Bài viết chứa nội dung quảng cáo rác (spam) và dịch vụ trái phép.', 0, 0, 7, NOW(), NOW()),
(10, 'Bản nháp: Tìm hiểu về WebSockets và Realtime Notifications', 'Ghi chú nghiên cứu về STOMP protocol và Spring WebSocket.', '<p>Đang soạn thảo nội dung tích hợp WebSocket với Angular client...</p>', 'DRAFT', NULL, 0, 0, 3, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 6. COMMENTS
INSERT INTO comments (id, content, creator_id, blog_id, parent_id, status, created_date, last_modified_date) VALUES
(1, 'Bài viết rất hay và chi tiết! Cảm ơn anh John Doe đã chia sẻ kiến thức Spring Boot 3.', 4, 1, NULL, 'ACTIVE', NOW(), NOW()),
(2, 'Cảm ơn Alice! Nếu em có thắc mắc gì về Spring Security 6 cứ comment ở dưới nhé.', 3, 1, 1, 'ACTIVE', NOW(), NOW()),
(3, 'Angular Signals thực sự giúp việc render UI mượt mà hơn hẳn so with ChangeDetectionStrategy.Default.', 5, 2, NULL, 'ACTIVE', NOW(), NOW()),
(4, 'Đồng ý với anh Tech Lead! Signals đơn giản hóa code rất nhiều.', 4, 2, 3, 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 7. COMMENT LIKES
INSERT INTO comment_likes (id, liked, comment_id, user_id, created_date, modified_date) VALUES
(1, true, 1, 5, NOW(), NOW()),
(2, true, 3, 3, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 8. LIKES
INSERT INTO likes (id, liked, blog_id, user_id, created_date, modified_date) VALUES
(1, true, 1, 4, NOW(), NOW()),
(2, true, 1, 5, NOW(), NOW()),
(3, true, 2, 3, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 9. BOOKMARKS
INSERT INTO bookmarks (id, user_id, blog_id, status, created_date, modified_date) VALUES
(1, 4, 1, 'ACTIVE', NOW(), NOW()),
(2, 3, 2, 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 10. SHARES
INSERT INTO shares (id, status, provider, blog_id, user_id, created_date, modified_date) VALUES
(1, 'ACTIVE', 'FACEBOOK', 1, 4, NOW(), NOW()),
(2, 'ACTIVE', 'LINKEDIN', 1, 5, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 11. VIEWS
INSERT INTO views (id, blog_id, user_id, ip_address, created_date) VALUES
(1, 1, 4, '192.168.1.10', NOW()),
(2, 1, 5, '192.168.1.20', NOW()),
(3, 2, 3, '192.168.1.30', NOW())
ON CONFLICT (id) DO NOTHING;

-- 12. REPORTS
INSERT INTO reports (id, target_type, target_id, reason, reporter_id, status, admin_notes, created_date, modified_date) VALUES
(1, 'BLOG', 7, 'Bài viết có dấu hiệu trùng lặp nội dung từ nguồn khác.', 3, 'PENDING', NULL, NOW(), NOW()),
(2, 'COMMENT', 1, 'Bình luận chứa liên kết không an toàn.', 4, 'PENDING', NULL, NOW(), NOW()),
(3, 'USER', 7, 'Tài khoản giả mạo thương hiệu.', 5, 'RESOLVED_ACCEPTED', 'Đã thực hiện khóa tài khoản vĩnh viễn.', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 13. NOTIFICATIONS
INSERT INTO notifications (id, recipient_id, title, content, type, is_read, related_url, created_date) VALUES
(1, 3, 'Bài viết đã được duyệt', 'Bài viết Hướng dẫn lập trình Java Spring Boot 3 từ A đến Z của bạn đã được phê duyệt.', 'MODERATION', true, '/blog/1', NOW()),
(2, 3, 'Bình luận mới', 'Alice Smith đã bình luận bài viết của bạn.', 'INTERACTION', false, '/blog/1', NOW()),
(3, 4, 'Chào mừng thành viên mới', 'Chào mừng bạn gia nhập hệ thống Blog Platform! Hãy khám phá các bài viết nổi bật hôm nay.', 'SYSTEM', false, '/blogs', NOW())
ON CONFLICT (id) DO NOTHING;
