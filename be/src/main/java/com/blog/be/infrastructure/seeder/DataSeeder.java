package com.blog.be.infrastructure.seeder;

import com.blog.be.admin.domain.entity.Report;
import com.blog.be.admin.domain.enums.ReportStatus;
import com.blog.be.admin.domain.enums.ReportTargetType;
import com.blog.be.admin.domain.repository.ReportRepository;
import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.Role;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.identity.domain.entity.UserRole;
import com.blog.be.identity.domain.enums.RoleStatus;
import com.blog.be.identity.domain.enums.UserRoleStatus;
import com.blog.be.identity.domain.enums.UserStatus;
import com.blog.be.identity.domain.repository.RoleRepository;
import com.blog.be.identity.domain.repository.UserRepository;
import com.blog.be.identity.domain.repository.UserRoleRepository;
import com.blog.be.interaction.domain.entity.*;
import com.blog.be.interaction.domain.enums.BookmarkStatus;
import com.blog.be.interaction.domain.enums.CommentStatus;
import com.blog.be.interaction.domain.enums.ShareStatus;
import com.blog.be.interaction.domain.repository.*;
import com.blog.be.notification.domain.entity.Notification;
import com.blog.be.notification.domain.enums.NotificationType;
import com.blog.be.notification.domain.repository.NotificationRepository;
import com.blog.be.storage.domain.entity.Image;
import com.blog.be.storage.domain.enums.ImageStatus;
import com.blog.be.storage.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ShareRepository shareRepository;
    private final ViewRepository viewRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Dữ liệu hệ thống đã tồn tại. Bỏ qua DataSeeder.");
            return;
        }

        log.info("Đang khởi tạo dữ liệu mẫu cho hệ thống...");
        seedData();
        log.info("Hoàn tất khởi tạo dữ liệu mẫu!");
    }

    @Transactional
    public void seedData() {
        // 1. Roles
        Role roleAdmin = roleRepository.save(Role.builder().name("ROLE_ADMIN").status(RoleStatus.ACTIVE).build());
        Role roleUser = roleRepository.save(Role.builder().name("ROLE_USER").status(RoleStatus.ACTIVE).build());
        Role roleMod = roleRepository.save(Role.builder().name("ROLE_MODERATOR").status(RoleStatus.ACTIVE).build());

        // 2. Images (Avatars & Covers)
        Image avatarAdmin = imageRepository.save(Image.builder()
                .url("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150")
                .description("Avatar Admin")
                .contentType("image/jpeg")
                .status(ImageStatus.ACTIVE)
                .build());

        Image avatarJohn = imageRepository.save(Image.builder()
                .url("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150")
                .description("Avatar John Doe")
                .contentType("image/jpeg")
                .status(ImageStatus.ACTIVE)
                .build());

        Image avatarAlice = imageRepository.save(Image.builder()
                .url("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150")
                .description("Avatar Alice Smith")
                .contentType("image/jpeg")
                .status(ImageStatus.ACTIVE)
                .build());

        Image avatarTechLead = imageRepository.save(Image.builder()
                .url("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150")
                .description("Avatar Tech Lead")
                .contentType("image/jpeg")
                .status(ImageStatus.ACTIVE)
                .build());

        String defaultPassword = passwordEncoder.encode("Password123!");

        // 3. Users
        User adminUser = User.builder()
                .email("admin@blog.com")
                .password(defaultPassword)
                .phone("0988888888")
                .bio("Quản trị viên hệ thống Blog Platform. Đam mê công nghệ và kiến trúc phần mềm.")
                .birthDate(LocalDate.of(1990, 1, 1))
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .avatar(avatarAdmin)
                .build();
        adminUser = userRepository.save(adminUser);
        userRoleRepository.save(UserRole.builder().user(adminUser).role(roleAdmin).status(UserRoleStatus.ACTIVE).build());

        User modUser = User.builder()
                .email("mod@blog.com")
                .password(defaultPassword)
                .phone("0977777777")
                .bio("Kểm duyệt viên nội dung. Đảm bảo chất lượng các bài viết trên hệ thống.")
                .birthDate(LocalDate.of(1992, 5, 15))
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .build();
        modUser = userRepository.save(modUser);
        userRoleRepository.save(UserRole.builder().user(modUser).role(roleMod).status(UserRoleStatus.ACTIVE).build());

        User userJohn = User.builder()
                .email("john.doe@techblog.com")
                .password(defaultPassword)
                .phone("0912345678")
                .bio("Senior Fullstack Developer. Yêu thích Spring Boot, Angular và Microservices.")
                .birthDate(LocalDate.of(1995, 3, 20))
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .avatar(avatarJohn)
                .build();
        userJohn = userRepository.save(userJohn);
        userRoleRepository.save(UserRole.builder().user(userJohn).role(roleUser).status(UserRoleStatus.ACTIVE).build());

        User userAlice = User.builder()
                .email("alice.smith@dev.io")
                .password(defaultPassword)
                .phone("0923456789")
                .bio("Frontend Specialist & UI/UX enthusiast. Chuyên sâu Angular & CSS Animations.")
                .birthDate(LocalDate.of(1997, 8, 10))
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .avatar(avatarAlice)
                .build();
        userAlice = userRepository.save(userAlice);
        userRoleRepository.save(UserRole.builder().user(userAlice).role(roleUser).status(UserRoleStatus.ACTIVE).build());

        User userTechLead = User.builder()
                .email("tech.lead@company.com")
                .password(defaultPassword)
                .phone("0934567890")
                .bio("Solution Architect với 10 năm kinh nghiệm trong các hệ thống High Concurrency.")
                .birthDate(LocalDate.of(1988, 11, 25))
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .avatar(avatarTechLead)
                .build();
        userTechLead = userRepository.save(userTechLead);
        userRoleRepository.save(UserRole.builder().user(userTechLead).role(roleUser).status(UserRoleStatus.ACTIVE).build());

        User pendingUser = User.builder()
                .email("pending.user@test.com")
                .password(defaultPassword)
                .phone("0945678901")
                .bio("Tài khoản chưa kích hoạt email.")
                .enabled(false)
                .status(UserStatus.PENDING)
                .build();
        pendingUser = userRepository.save(pendingUser);
        userRoleRepository.save(UserRole.builder().user(pendingUser).role(roleUser).status(UserRoleStatus.ACTIVE).build());

        User bannedUser = User.builder()
                .email("banned.user@test.com")
                .password(defaultPassword)
                .phone("0956789012")
                .bio("Tài khoản vi phạm quy định cộng đồng.")
                .enabled(true)
                .status(UserStatus.BANNED)
                .build();
        bannedUser = userRepository.save(bannedUser);
        userRoleRepository.save(UserRole.builder().user(bannedUser).role(roleUser).status(UserRoleStatus.ACTIVE).build());

        // 4. Blogs (PUBLISHED, PENDING, REJECTED, DRAFT)
        List<Blog> publishedBlogs = new ArrayList<>();

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Hướng dẫn lập trình Java Spring Boot 3 từ A đến Z")
                .description("Tìm hiểu toàn bộ tính năng mới trong Spring Boot 3, Spring Security 6 và AOT compilation.")
                .content("<p>Spring Boot 3 mang đến nhiều cải tiến mạnh mẽ vượt trội với Java 17+, GraalVM Native Image và Spring Security 6...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(1250)
                .shareCount(45)
                .user(userJohn)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Xây dựng ứng dụng Angular với Standalone Components & Signals")
                .description("Khám phá mô hình kiến trúc mới nhất của Angular giúp tối ưu hiệu năng và giản lược cấu hình NgModule.")
                .content("<p>Angular v16+ giới thiệu Signals và Standalone Components làm thay đổi hoàn toàn cách quản lý State...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(980)
                .shareCount(32)
                .user(userAlice)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Tối ưu hóa hiệu năng Database PostgreSQL trong các ứng dụng tải cao")
                .description("Chiến lược đánh Index, Partitioning, Connection Pooling với HikariCP và xử lý N+1 Query trong JPA.")
                .content("<p>Bài viết chia sẻ các kỹ thuật thực chiến giúp PostgreSQL đạt 10.000 QPS mà không bị tắc nghẽn I/O...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(1560)
                .shareCount(88)
                .user(userTechLead)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Thiết kế hệ thống Microservices & RESTful API chuẩn REST Client")
                .description("Nguyên tắc thiết kế API rõ ràng, phiên bản hóa API, xử lý lỗi toàn cục và OpenAPI documentation.")
                .content("<p>Một RESTful API chất lượng cần đáp ứng tính đồng nhất trong mã lỗi, chuẩn hóa response format...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(740)
                .shareCount(19)
                .user(userJohn)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Tích hợp lưu trữ hình ảnh với Cloudflare R2 Storage & Presigned URL")
                .description("Giải pháp lưu trữ media tối ưu chi phí, không tốn băng thông egress và bảo mật bằng presigned request.")
                .content("<p>Sử dụng S3 API client kết nối Cloudflare R2 để tải ảnh trực tiếp từ trình duyệt giúp giảm tải server backend...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(620)
                .shareCount(15)
                .user(userAlice)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Bảo mật ứng dụng Web với OAuth2, JWT và Spring Security")
                .description("Phân tích cơ chế mã hóa JWT Stateless, Token Refresh Cookie và tích hợp Google/Facebook Login.")
                .content("<p>Định danh người dùng qua OAuth2 và Resource Server JWT giúp hệ thống mở rộng linh hoạt theo kiến trúc Microservices...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(2100)
                .shareCount(110)
                .user(userTechLead)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Chiến lược Caching hiệu quả sử dụng Redis trong Spring Boot")
                .description("Sử dụng @Cacheable, Redis Template và giải quyết bài toán Cache Avalanche / Cache Stampede.")
                .content("<p>Bộ nhớ đệm Redis giúp giảm thời gian phản hồi API từ 500ms xuống chỉ còn vài millisecond...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(1120)
                .shareCount(40)
                .user(userJohn)
                .build()));

        publishedBlogs.add(blogRepository.save(Blog.builder()
                .title("Áp dụng Clean Architecture & Domain-Driven Design trong Java")
                .description("Cách phân tách 4 lớp API -> Application -> Domain -> Infrastructure để giữ codebase sạch sẽ.")
                .content("<p>DDD giúp doanh nghiệp làm chủ độ phức tạp của phần mềm bằng cách cô lập Domain Model khỏi Framework...</p>")
                .status(BlogStatus.PUBLISHED)
                .viewCount(890)
                .shareCount(27)
                .user(userTechLead)
                .build()));

        // PENDING Blogs (Chờ admin duyệt)
        Blog pendingBlog1 = blogRepository.save(Blog.builder()
                .title("Đánh giá các mô hình AI mã nguồn mở năm 2026")
                .description("Phân tích năng lực của Llama 4, DeepSeek R1 và các mô hình LLM chạy local.")
                .content("<p>Các mô hình AI mã nguồn mở đang tiến rất gần đến trình độ của các mô hình thương mại hàng đầu...</p>")
                .status(BlogStatus.PENDING)
                .user(userAlice)
                .build());

        Blog pendingBlog2 = blogRepository.save(Blog.builder()
                .title("Kinh nghiệm chuyển dịch hệ thống từ Monolith sang Microservices")
                .description("Bài học kinh nghiệm thực tế về phân tách database, saga pattern và distributed tracing.")
                .content("<p>Chuyển đổi sang Microservices không chỉ là thay đổi công nghệ mà là thay đổi cấu trúc tổ chức team...</p>")
                .status(BlogStatus.PENDING)
                .user(userJohn)
                .build());

        // REJECTED Blogs
        blogRepository.save(Blog.builder()
                .title("Quảng cáo dịch vụ tăng tương tác không rõ nguồn gốc")
                .description("Cung cấp dịch vụ tăng follow và like tự động giá rẻ.")
                .content("<p>Truy cập ngay trang web xyz để nhận ưu đãi mua view giá rẻ...</p>")
                .status(BlogStatus.REJECTED)
                .rejectionReason("Bài viết chứa nội dung quảng cáo rác (spam) và dịch vụ trái phép.")
                .user(bannedUser)
                .build());

        // DRAFT Blogs
        blogRepository.save(Blog.builder()
                .title("Bản nháp: Tìm hiểu về WebSockets và Realtime Notifications")
                .description("Ghi chú nghiên cứu về STOMP protocol và Spring WebSocket.")
                .content("<p>Đang soạn thảo nội dung tích hợp WebSocket với Angular client...</p>")
                .status(BlogStatus.DRAFT)
                .user(userJohn)
                .build());

        // 5. Comments & Replies
        Blog blog1 = publishedBlogs.get(0);
        Blog blog2 = publishedBlogs.get(1);

        Comment comment1 = commentRepository.save(Comment.builder()
                .content("Bài viết rất hay và chi tiết! Cảm ơn anh John Doe đã chia sẻ kiến thức Spring Boot 3.")
                .blog(blog1)
                .creator(userAlice)
                .status(CommentStatus.ACTIVE)
                .build());

        commentRepository.save(Comment.builder()
                .content("Cảm ơn Alice! Nếu em có thắc mắc gì về Spring Security 6 cứ comment ở dưới nhé.")
                .blog(blog1)
                .creator(userJohn)
                .parent(comment1)
                .status(CommentStatus.ACTIVE)
                .build());

        Comment comment2 = commentRepository.save(Comment.builder()
                .content("Angular Signals thực sự giúp việc render UI mượt mà hơn hẳn so with ChangeDetectionStrategy.Default.")
                .blog(blog2)
                .creator(userTechLead)
                .status(CommentStatus.ACTIVE)
                .build());

        commentRepository.save(Comment.builder()
                .content("Đồng ý với anh Tech Lead! Signals đơn giản hóa code rất nhiều.")
                .blog(blog2)
                .creator(userAlice)
                .parent(comment2)
                .status(CommentStatus.ACTIVE)
                .build());

        // 6. Comment Likes
        commentLikeRepository.save(CommentLike.builder().comment(comment1).user(userTechLead).liked(true).build());
        commentLikeRepository.save(CommentLike.builder().comment(comment2).user(userJohn).liked(true).build());

        // 7. Likes
        likeRepository.save(Like.builder().blog(blog1).user(userAlice).liked(true).build());
        likeRepository.save(Like.builder().blog(blog1).user(userTechLead).liked(true).build());
        likeRepository.save(Like.builder().blog(blog2).user(userJohn).liked(true).build());

        // 8. Bookmarks
        bookmarkRepository.save(Bookmark.builder().blog(blog1).user(userAlice).status(BookmarkStatus.ACTIVE).build());
        bookmarkRepository.save(Bookmark.builder().blog(blog2).user(userJohn).status(BookmarkStatus.ACTIVE).build());

        // 9. Shares
        shareRepository.save(Share.builder().blog(blog1).author(userAlice).provider("FACEBOOK").status(ShareStatus.ACTIVE).build());
        shareRepository.save(Share.builder().blog(blog1).author(userTechLead).provider("LINKEDIN").status(ShareStatus.ACTIVE).build());

        // 10. Views
        viewRepository.save(View.builder().blog(blog1).user(userAlice).ipAddress("192.168.1.10").build());
        viewRepository.save(View.builder().blog(blog1).user(userTechLead).ipAddress("192.168.1.20").build());
        viewRepository.save(View.builder().blog(blog2).user(userJohn).ipAddress("192.168.1.30").build());

        // 11. Reports (ADMIN verification)
        reportRepository.save(Report.builder()
                .targetType(ReportTargetType.BLOG)
                .targetId(pendingBlog1.getId())
                .reason("Bài viết có dấu hiệu trùng lặp nội dung từ nguồn khác.")
                .reporter(userJohn)
                .status(ReportStatus.PENDING)
                .build());

        reportRepository.save(Report.builder()
                .targetType(ReportTargetType.COMMENT)
                .targetId(comment1.getId())
                .reason("Bình luận chứa liên kết không an toàn.")
                .reporter(userAlice)
                .status(ReportStatus.PENDING)
                .build());

        reportRepository.save(Report.builder()
                .targetType(ReportTargetType.USER)
                .targetId(bannedUser.getId())
                .reason("Tài khoản giả mạo thương hiệu.")
                .reporter(userTechLead)
                .status(ReportStatus.RESOLVED_ACCEPTED)
                .adminNotes("Đã thực hiện khóa tài khoản vĩnh viễn.")
                .build());

        // 12. Notifications
        notificationRepository.save(Notification.builder()
                .recipient(userJohn)
                .title("Bài viết đã được duyệt")
                .content("Bài viết 'Hướng dẫn lập trình Java Spring Boot 3 từ A đến Z' của bạn đã được phê duyệt.")
                .type(NotificationType.MODERATION)
                .isRead(true)
                .relatedUrl("/blog/" + blog1.getId())
                .build());

        notificationRepository.save(Notification.builder()
                .recipient(userJohn)
                .title("Bình luận mới")
                .content("Alice Smith đã bình luận bài viết của bạn.")
                .type(NotificationType.INTERACTION)
                .isRead(false)
                .relatedUrl("/blog/" + blog1.getId())
                .build());

        notificationRepository.save(Notification.builder()
                .recipient(userAlice)
                .title("Chào mừng thành viên mới")
                .content("Chào mừng bạn gia nhập hệ thống Blog Platform! Hãy khám phá các bài viết nổi bật hôm nay.")
                .type(NotificationType.SYSTEM)
                .isRead(false)
                .relatedUrl("/blogs")
                .build());
    }
}
