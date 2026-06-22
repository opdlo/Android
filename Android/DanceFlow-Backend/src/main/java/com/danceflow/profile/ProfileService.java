package com.danceflow.profile;

import com.danceflow.dto.*;
import com.danceflow.entity.*;
import com.danceflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VideoResourceRepository videoResourceRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public FavoriteListResponse getFavorites() {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByIdDesc(userId);

        List<FavoriteItem> items = favorites.stream()
                .map(this::convertToFavoriteItem)
                .collect(Collectors.toList());

        return new FavoriteListResponse(items, items.size());
    }

    public String uploadAvatar(MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        // 保存文件
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, "avatars");
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        String avatarUrl = "/uploads/avatars/" + fileName;

        // 更新用户头像
        User user = userRepository.findById(userId).orElseThrow();
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    private FavoriteItem convertToFavoriteItem(Favorite favorite) {
        String title = null;
        String coverUrl = null;

        if ("post".equals(favorite.getType()) && favorite.getPostId() != null) {
            Post post = postRepository.findById(favorite.getPostId()).orElse(null);
            if (post != null) {
                title = post.getContent().substring(0, Math.min(50, post.getContent().length()));
                // TODO: 解析图片获取封面
            }
        } else if ("video".equals(favorite.getType()) && favorite.getVideoId() != null) {
            com.danceflow.entity.VideoResource video =
                    videoResourceRepository.findById(favorite.getVideoId()).orElse(null);
            if (video != null) {
                title = video.getTitle();
                coverUrl = video.getCoverUrl();
            }
        }

        return new FavoriteItem(
                favorite.getId(),
                favorite.getType(),
                favorite.getPostId(),
                favorite.getVideoId(),
                title,
                coverUrl,
                favorite.getCreatedAt()
        );
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    public String uploadBackground(MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        if (userId == null) throw new RuntimeException("Not logged in");

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, "backgrounds");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        String backgroundUrl = "/uploads/backgrounds/" + fileName;
        User user = userRepository.findById(userId).orElseThrow();
        user.setBackgroundUrl(backgroundUrl);
        userRepository.save(user);
        return backgroundUrl;
    }
}

