package com.danceflow.community;

import com.danceflow.dto.*;
import com.danceflow.entity.*;
import com.danceflow.repository.*;
import com.danceflow.security.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRecordRepository likeRecordRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PostListResponse getPosts(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage;

        if (search != null && !search.isEmpty()) {
            postsPage = postRepository.searchPosts(search, pageable);
        } else {
            postsPage = postRepository.findAllByOrderByIdDesc(pageable);
        }

        Long userId = getCurrentUserId();

        List<PostResponse> postResponses = postsPage.getContent().stream()
                .map(post -> convertToPostResponse(post, userId))
                .collect(Collectors.toList());

        return new PostListResponse(postResponses, (int) postsPage.getTotalElements(), page, size);
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        Long userId = getCurrentUserId();
        return convertToPostResponse(post, userId);
    }

    public PostResponse createPost(CreatePostRequest request) {
        Long userId = getCurrentUserId();

        Post post = new Post();
        post.setAuthorId(userId);
        post.setContent(request.getContent());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                post.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (Exception e) {
                post.setImages(null);
            }
        }

        post.setVideoUrl(request.getVideoUrl());
        post.setLikesCount(0);
        post.setCommentsCount(0);

        post = postRepository.save(post);

        return convertToPostResponse(post, userId);
    }

    @Transactional
    public void likePost(Long postId) {
        Long userId = getCurrentUserId();

        Optional<LikeRecord> existingLike = likeRecordRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            throw new RuntimeException("已经点赞过了");
        }

        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setPostId(postId);
        likeRecordRepository.save(likeRecord);

        Post post = postRepository.findById(postId).orElseThrow();
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        Long userId = getCurrentUserId();

        likeRecordRepository.deleteByUserIdAndPostId(userId, postId);

        Post post = postRepository.findById(postId).orElseThrow();
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        postRepository.save(post);
    }

    @Transactional
    public void favoritePost(Long postId) {
        Long userId = getCurrentUserId();

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            throw new RuntimeException("已经收藏过了");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setType("post");
        favorite.setPostId(postId);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void unfavoritePost(Long postId) {
        Long userId = getCurrentUserId();

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndPostId(userId, postId);
        existing.ifPresent(favoriteRepository::delete);
    }

    public List<CommentResponse> getComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Long userId = getCurrentUserId();

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setContent(request.getContent());

        comment = commentRepository.save(comment);

        // 更新评论数
        Post post = postRepository.findById(postId).orElseThrow();
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        return convertToCommentResponse(comment);
    }

    private PostResponse convertToPostResponse(Post post, Long currentUserId) {
        User author = userRepository.findById(post.getAuthorId()).orElse(null);

        AuthorInfo authorInfo = null;
        if (author != null) {
            authorInfo = new AuthorInfo(
                    author.getId(),
                    author.getUsername(),
                    author.getAvatarUrl()
            );
        }

        List<String> images = null;
        if (post.getImages() != null) {
            try {
                images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                images = new ArrayList<>();
            }
        }

        boolean isLiked = currentUserId != null &&
                likeRecordRepository.findByUserIdAndPostId(currentUserId, post.getId()).isPresent();

        boolean isFavorited = currentUserId != null &&
                favoriteRepository.findByUserIdAndPostId(currentUserId, post.getId()).isPresent();

        return new PostResponse(
                post.getId(),
                authorInfo,
                post.getContent(),
                images,
                post.getVideoUrl(),
                post.getLikesCount(),
                post.getCommentsCount(),
                isLiked,
                isFavorited,
                post.getCreatedAt()
        );
    }

    private CommentResponse convertToCommentResponse(Comment comment) {
        User author = userRepository.findById(comment.getAuthorId()).orElse(null);

        AuthorInfo authorInfo = null;
        if (author != null) {
            authorInfo = new AuthorInfo(
                    author.getId(),
                    author.getUsername(),
                    author.getAvatarUrl()
            );
        }

        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                authorInfo,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    public PostListResponse getUserPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findByAuthorId(userId, pageable);

        List<PostResponse> postResponses = postsPage.getContent().stream()
                .map(post -> convertToPostResponse(post, userId))
                .collect(Collectors.toList());

        return new PostListResponse(postResponses, (int) postsPage.getTotalElements(), page, size);
    }
}

