package com.blog.be.interaction.application;

import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.exception.BlogNotFoundException;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.RecordViewRequest;
import com.blog.be.interaction.api.dto.ViewResponse;
import com.blog.be.interaction.domain.entity.View;
import com.blog.be.interaction.domain.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewServiceImpl implements ViewService {

    private final ViewRepository viewRepository;
    private final BlogRepository blogRepository;

    @Override
    @Transactional
    public ViewResponse recordView(User currentUser, String ipAddress, RecordViewRequest request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        boolean alreadyViewed = false;

        if (currentUser != null && currentUser.getId() != null) {
            alreadyViewed = viewRepository.existsByBlogIdAndUserIdAndCreatedDateAfter(blog.getId(), currentUser.getId(), since);
        } else if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            alreadyViewed = viewRepository.existsByBlogIdAndIpAddressAndCreatedDateAfter(blog.getId(), ipAddress, since);
        }

        if (alreadyViewed) {
            return ViewResponse.builder()
                    .blogId(blog.getId())
                    .recorded(false)
                    .totalViews(blog.getViewCount())
                    .build();
        }

        View view = View.builder()
                .blog(blog)
                .user(currentUser != null && currentUser.getId() != null ? currentUser : null)
                .ipAddress(ipAddress)
                .build();

        viewRepository.save(view);

        blogRepository.incrementViewCount(blog.getId());
        int newTotalViews = blog.getViewCount() + 1;

        return ViewResponse.builder()
                .blogId(blog.getId())
                .recorded(true)
                .totalViews(newTotalViews)
                .build();
    }
}
