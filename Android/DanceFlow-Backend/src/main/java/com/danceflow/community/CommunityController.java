package com.danceflow.community;

import com.danceflow.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        try {
            PostListResponse response = communityService.getPosts(page, size, search);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        try {
            PostResponse response = communityService.getPost(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
        try {
            PostResponse response = communityService.createPost(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        try {
            communityService.likePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<?> unlikePost(@PathVariable Long id) {
        try {
            communityService.unlikePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/favorite")
    public ResponseEntity<?> favoritePost(@PathVariable Long id) {
        try {
            communityService.favoritePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}/favorite")
    public ResponseEntity<?> unfavoritePost(@PathVariable Long id) {
        try {
            communityService.unfavoritePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(communityService.getComments(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long id,
            @RequestBody CreateCommentRequest request
    ) {
        try {
            CommentResponse response = communityService.createComment(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<?> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            PostListResponse response = communityService.getUserPosts(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

