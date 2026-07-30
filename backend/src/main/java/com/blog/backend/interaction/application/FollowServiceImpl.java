package com.blog.backend.interaction.application;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.interaction.api.dto.FollowStatusResponse;
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
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");
        }
        if (currentUser.getId().equals(followingId)) {
            throw new IllegalArgumentException("Không thể tự theo dõi chính mình");
        }

        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tác giả"));

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(), followingId);
        boolean isFollowing;

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            isFollowing = false;
        } else {
            Follow newFollow = Follow.builder()
                    .follower(currentUser)
                    .following(followingUser)
                    .build();
            followRepository.save(newFollow);
            isFollowing = true;
        }

        long followersCount = followRepository.countByFollowingId(followingId);
        return new FollowStatusResponse(isFollowing, followersCount);
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
