package com.blog.backend.interaction.application.impl;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.interaction.api.dto.FollowStatusResponse;
import com.blog.backend.interaction.application.itf.FollowService;
import com.blog.backend.interaction.domain.entity.Follow;
import com.blog.backend.interaction.domain.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FollowStatusResponse toggleFollow(User currentUser, Long followingId) {
        // Kiểm tra quyền truy cập: Người dùng phải đăng nhập để theo dõi
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");
        }
        // Không cho phép người dùng tự theo dõi chính mình
        if (currentUser.getId().equals(followingId)) {
            throw new IllegalArgumentException("Không thể tự theo dõi chính mình");
        }

        // Lấy thông tin người dùng đang được theo dõi từ DB, nếu không tồn tại -> ném
        // ngoại lệ
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả"));

        // Kiểm tra xem người dùng hiện tại đã theo dõi người dùng này chưa
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(),
                followingId);
        boolean isFollowing;

        long followersCount = followRepository.countByFollowingId(followingId);

        if (existingFollow.isEmpty()) {
            Follow newFollow = Follow.builder()
                    .follower(currentUser)
                    .following(followingUser)
                    .build();
            followRepository.save(newFollow);
            isFollowing = true;
            return new FollowStatusResponse(isFollowing, followersCount + 1);
        }

        followRepository.delete(existingFollow.get());
        isFollowing = false;
        return new FollowStatusResponse(isFollowing, followersCount - 1);
    }

    @Override
    public FollowStatusResponse getFollowStatus(User currentUser, Long followingId) {
        long followersCount = followRepository.countByFollowingId(followingId);
        boolean isFollowing = false;
        if (currentUser != null && currentUser.getId() != null) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), followingId);
        }
        return new FollowStatusResponse(isFollowing, followersCount);
    }
}
