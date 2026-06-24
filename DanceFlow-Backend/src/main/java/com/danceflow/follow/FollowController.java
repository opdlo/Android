package com.danceflow.follow;

import com.danceflow.dto.FollowCountResponse;
import com.danceflow.dto.FollowResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> follow(@PathVariable Long userId) {
        try {
            FollowResponse response = followService.follow(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollow(@PathVariable Long userId) {
        try {
            followService.unfollow(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followService.getFollowers(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followService.getFollowing(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<?> getFollowCount(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followService.getFollowCount(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check/{followeeId}")
    public ResponseEntity<?> isFollowing(@PathVariable Long followeeId) {
        try {
            Long currentUserId = getCurrentUserId();
            boolean following = followService.isFollowing(currentUserId, followeeId);
            return ResponseEntity.ok(java.util.Map.of("following", following));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}