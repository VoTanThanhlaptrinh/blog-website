package com.blog.backend.infrastructure.seeder;

import com.blog.backend.admin.domain.repository.SystemSettingRepository;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.entity.Category;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.content.domain.repository.CategoryRepository;
import com.blog.backend.identity.domain.entity.Role;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.entity.UserRole;
import com.blog.backend.identity.domain.enums.RoleStatus;
import com.blog.backend.identity.domain.enums.UserRoleStatus;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.identity.domain.repository.RoleRepository;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.identity.domain.repository.UserRoleRepository;
import com.blog.backend.interaction.domain.entity.Bookmark;
import com.blog.backend.interaction.domain.entity.Comment;
import com.blog.backend.interaction.domain.entity.Follow;
import com.blog.backend.interaction.domain.entity.Like;
import com.blog.backend.interaction.domain.enums.CommentStatus;
import com.blog.backend.interaction.domain.repository.BookmarkRepository;
import com.blog.backend.interaction.domain.repository.CommentRepository;
import com.blog.backend.interaction.domain.repository.FollowRepository;
import com.blog.backend.interaction.domain.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FollowRepository followRepository;
    
    // Using a fake password encoder for seeding to avoid needing a specific bean if it's not defined here
    // But normally we'd inject PasswordEncoder. I will assume PasswordEncoder is available.
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {}
    
    // @Override
    // public void run(String... args) throws Exception {
    //     if (roleRepository.count() > 0) {
    //         log.info("Database already seeded. Skipping...");
    //         return;
    //     }

    //     log.info("Starting database seeding...");
    //     Faker faker = new Faker();
    //     Random random = new Random();

    //     // 1. Seed Roles
    //     log.info("Seeding Roles...");
    //     Role adminRole = Role.builder().name("ROLE_ADMIN").status(RoleStatus.ACTIVE).build();
    //     Role userRole = Role.builder().name("ROLE_USER").status(RoleStatus.ACTIVE).build();
    //     Role creatorRole = Role.builder().name("ROLE_CONTENT_CREATOR").status(RoleStatus.ACTIVE).build();
        
    //     roleRepository.saveAll(List.of(adminRole, userRole, creatorRole));

    //     // 2. Seed Users
    //     log.info("Seeding Users...");
    //     List<User> users = new ArrayList<>();
    //     String defaultPassword = passwordEncoder.encode("Password@123");
        
    //     // Admin user
    //     User adminUser = User.builder()
    //             .email("admin@blog.com")
    //             .password(defaultPassword)
    //             .enabled(true)
    //             .status(UserStatus.ACTIVE)
    //             .bio(faker.lorem().sentence())
    //             .phone(faker.phoneNumber().cellPhone())
    //             .birthDate(LocalDate.of(1990, 1, 1))
    //             .build();
    //     users.add(adminUser);

    //     // Regular users
    //     for (int i = 0; i < 15; i++) {
    //         User user = User.builder()
    //                 .email(faker.internet().emailAddress())
    //                 .password(defaultPassword)
    //                 .enabled(true)
    //                 .status(UserStatus.ACTIVE)
    //                 .bio(faker.lorem().paragraph())
    //                 .phone(faker.phoneNumber().cellPhone())
    //                 .birthDate(faker.date().birthday().toLocalDateTime().toLocalDate())
    //                 .build();
    //         users.add(user);
    //     }
    //     userRepository.saveAll(users);

    //     // 3. Seed User Roles
    //     log.info("Seeding User Roles...");
    //     List<UserRole> userRoles = new ArrayList<>();
    //     userRoles.add(UserRole.builder().user(users.get(0)).role(adminRole).status(UserRoleStatus.ACTIVE).build());
    //     for (int i = 1; i < users.size(); i++) {
    //         Role role = (i % 3 == 0) ? creatorRole : userRole;
    //         userRoles.add(UserRole.builder().user(users.get(i)).role(role).status(UserRoleStatus.ACTIVE).build());
    //     }
    //     userRoleRepository.saveAll(userRoles);

    //     // 4. Seed Categories
    //     log.info("Seeding Categories...");
    //     List<String> categoryNames = List.of("Programming", "Artificial Intelligence", "Lifestyle", "Tech News", "Startups", "Web Development", "Mobile Apps");
    //     List<Category> categories = new ArrayList<>();
    //     for (String name : categoryNames) {
    //         Category category = Category.builder()
    //                 .name(name)
    //                 .slug(name.toLowerCase().replace(" ", "-"))
    //                 .description(faker.lorem().sentence())
    //                 .build();
    //         categories.add(category);
    //     }
    //     categoryRepository.saveAll(categories);

    //     // 5. Seed Blogs
    //     log.info("Seeding Blogs...");
    //     List<Blog> blogs = new ArrayList<>();
    //     for (int i = 0; i < 30; i++) {
    //         User author = users.get(random.nextInt(users.size()));
    //         Category category = categories.get(random.nextInt(categories.size()));
    //         Blog blog = Blog.builder()
    //                 .title(faker.book().title())
    //                 .description(faker.lorem().paragraph())
    //                 .content(faker.lorem().paragraphs(5).toString())
    //                 .status(BlogStatus.PUBLISHED)
    //                 .viewCount(random.nextInt(1000))
    //                 .shareCount(random.nextInt(100))
    //                 .thumbnailUrl("https://picsum.photos/seed/" + i + "/800/400")
    //                 .user(author)
    //                 .category(category)
    //                 .build();
    //         blogs.add(blog);
    //     }
    //     blogRepository.saveAll(blogs);

    //     // 6. Seed Comments
    //     log.info("Seeding Comments...");
    //     List<Comment> comments = new ArrayList<>();
    //     for (int i = 0; i < 50; i++) {
    //         Blog blog = blogs.get(random.nextInt(blogs.size()));
    //         User creator = users.get(random.nextInt(users.size()));
    //         Comment comment = Comment.builder()
    //                 .content(faker.lorem().sentence())
    //                 .blog(blog)
    //                 .creator(creator)
    //                 .status(CommentStatus.ACTIVE)
    //                 .build();
    //         comments.add(comment);
    //     }
    //     commentRepository.saveAll(comments);

    //     // 7. Seed Interactions (Likes, Bookmarks, Follows)
    //     log.info("Seeding Interactions...");
    //     List<Like> likes = new ArrayList<>();
    //     List<Bookmark> bookmarks = new ArrayList<>();
    //     for (int i = 0; i < 100; i++) {
    //         Blog blog = blogs.get(random.nextInt(blogs.size()));
    //         User user = users.get(random.nextInt(users.size()));
    //         likes.add(Like.builder().blog(blog).user(user).build());
            
    //         if (i % 3 == 0) {
    //             bookmarks.add(Bookmark.builder().blog(blog).user(user).build());
    //         }
    //     }
    //     likeRepository.saveAll(likes);
    //     bookmarkRepository.saveAll(bookmarks);

    //     List<Follow> follows = new ArrayList<>();
    //     for (int i = 0; i < 20; i++) {
    //         User follower = users.get(random.nextInt(users.size()));
    //         User following = users.get(random.nextInt(users.size()));
    //         if (!follower.getId().equals(following.getId())) {
    //             follows.add(Follow.builder().follower(follower).following(following).build());
    //         }
    //     }
    //     followRepository.saveAll(follows);

    //     log.info("Database seeding completed successfully!");
    // }
}
