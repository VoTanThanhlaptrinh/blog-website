package com.blog.backend.interaction.application.impl;

import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CreateShareRequest;
import com.blog.backend.interaction.api.dto.ShareResponse;
import com.blog.backend.interaction.application.itf.ShareService;
import com.blog.backend.interaction.domain.entity.Share;
import com.blog.backend.interaction.domain.enums.ShareStatus;
import com.blog.backend.interaction.domain.repository.ShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareRepository shareRepository;
    private final BlogRepository blogRepository;

    @Override
    @Transactional
    public ShareResponse createShare(User currentUser, CreateShareRequest request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        Share share = Share.builder()
                .blog(blog)
                .author(currentUser) // May be null if anonymous
                .provider(request.getProvider())
                .status(ShareStatus.ACTIVE)
                .build();

        share = shareRepository.save(share);

        // Increment share count on Blog
        blogRepository.incrementShareCount(blog.getId());

        int totalShares = blog.getShareCount() + 1;

        return ShareResponse.builder()
                .id(share.getId())
                .blogId(blog.getId())
                .provider(share.getProvider())
                .totalShares(totalShares)
                .createdDate(share.getCreatedDate())
                .build();
    }
}
