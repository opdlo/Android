package com.danceflow.follow;

import com.danceflow.dto.FollowCountResponse;
import com.danceflow.dto.FollowResponse;
import com.danceflow.entity.Follow;
import com.danceflow.entity.User;
import com.danceflow.repository.FollowRepository;
import com.danceflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public FollowResponse follow(Long followeeId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        Optional<Follow> existing = followRepository.findByFollowerIdAndFolloweeId(currentUserId, followeeId);
        if (existing.isPresent()) {
            throw new RuntimeException("已经关注过了");
        }

        Follow follow = new Follow();
        follow.setFollowerId(currentUserId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);

        User followee = userRepository.findById(followeeId).orElseThrow(() -> new RuntimeException("用户不存在"));
        boolean isMutual = followRepository.existsByFollowerIdAndFolloweeId(followeeId, currentUserId);

        return new FollowResponse(follow.getId(), followee.getId(), followee.getUsername(),
                followee.getAvatarUrl(), follow.getCreatedAt(), isMutual);
    }

    @Transactional
    public void unfollow(Long followeeId) {
        Long currentUserId = getCurrentUserId();
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(currentUserId, followeeId)
                .orElseThrow(() -> new RuntimeException("未关注"));
        followRepository.delete(follow);
    }

    public List<FollowResponse> getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Follow> follows = followRepository.findByFolloweeId(userId);
        Long currentUserId = getCurrentUserId();
        return follows.stream().map(f -> {
            User follower = userRepository.findById(f.getFollowerId()).orElse(null);
            boolean isMutual = currentUserId != null && followRepository.existsByFollowerIdAndFolloweeId(currentUserId, f.getFollowerId());
            return new FollowResponse(f.getId(), f.getFollowerId(), follower != null ? follower.getUsername() : null,
                    follower != null ? follower.getAvatarUrl() : null, f.getCreatedAt(), isMutual);
        }).collect(Collectors.toList());
    }

    public List<FollowResponse> getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Follow> follows = followRepository.findByFollowerId(userId);
        Long currentUserId = getCurrentUserId();
        return follows.stream().map(f -> {
            User followee = userRepository.findById(f.getFolloweeId()).orElse(null);
            boolean isMutual = currentUserId != null && followRepository.existsByFollowerIdAndFolloweeId(currentUserId, f.getFolloweeId());
            return new FollowResponse(f.getId(), f.getFolloweeId(), followee != null ? followee.getUsername() : null,
                    followee != null ? followee.getAvatarUrl() : null, f.getCreatedAt(), isMutual);
        }).collect(Collectors.toList());
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public FollowCountResponse getFollowCount(Long userId) {
        long followingCount = followRepository.countByFollowerId(userId);
        long followersCount = followRepository.countByFolloweeId(userId);
        return new FollowCountResponse(followingCount, followersCount);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}