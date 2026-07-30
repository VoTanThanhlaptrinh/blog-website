package com.blog.backend.interaction.application;

import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.RecordViewRequest;
import com.blog.backend.interaction.api.dto.ViewResponse;
import com.blog.backend.interaction.domain.entity.View;
import com.blog.backend.interaction.domain.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service xử lý ghi nhận lượt xem (View) bài viết kèm cơ chế chống gian lận/spam view.
 */
@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

    private final ViewRepository viewRepository;
    private final BlogRepository blogRepository;

    /**
     * Ghi nhận 1 lượt xem cho bài viết.
     * Cơ chế Anti-Spam: Mỗi User (hoặc IP nếu chưa đăng nhập) chỉ được tính 1 lượt xem cho 1 bài viết trong vòng 24 giờ.
     */
    @Override
    @Transactional
    public ViewResponse recordView(User currentUser, String ipAddress, RecordViewRequest request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        // Mốc thời gian 24 giờ trước để kiểm tra trùng lặp
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        boolean alreadyViewed = false;

        // Nếu đã đăng nhập: kiểm tra theo userId trong 24h
        if (currentUser != null && currentUser.getId() != null) {
            alreadyViewed = viewRepository.existsByBlogIdAndUserIdAndCreatedDateAfter(blog.getId(), currentUser.getId(), since);
        } else if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            // Nếu là khách vãng lai: kiểm tra theo IP trong 24h
            alreadyViewed = viewRepository.existsByBlogIdAndIpAddressAndCreatedDateAfter(blog.getId(), ipAddress, since);
        }

        // Bỏ qua không tăng view nếu đã đọc trong 24h qua
        if (alreadyViewed) {
            return ViewResponse.builder()
                    .blogId(blog.getId())
                    .recorded(false)
                    .totalViews(blog.getViewCount())
                    .build();
        }

        // Lưu bản ghi View mới
        View view = View.builder()
                .blog(blog)
                .user(currentUser != null && currentUser.getId() != null ? currentUser : null)
                .ipAddress(ipAddress)
                .build();

        viewRepository.save(view);

        // Tăng atomic count view trong DB
        blogRepository.incrementViewCount(blog.getId());
        int newTotalViews = blog.getViewCount() + 1;

        return ViewResponse.builder()
                .blogId(blog.getId())
                .recorded(true)
                .totalViews(newTotalViews)
                .build();
    }
}
