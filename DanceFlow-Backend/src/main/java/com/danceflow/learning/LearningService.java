package com.danceflow.learning;

import com.danceflow.dto.VideoCategory;
import com.danceflow.dto.VideoListResponse;
import com.danceflow.dto.VideoResourceResponse;
import com.danceflow.entity.Favorite;
import com.danceflow.entity.VideoResource;
import com.danceflow.repository.FavoriteRepository;
import com.danceflow.repository.UserRepository;
import com.danceflow.repository.VideoCategoryRepository;
import com.danceflow.repository.VideoResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LearningService {

    @Autowired
    private VideoCategoryRepository videoCategoryRepository;

    @Autowired
    private VideoResourceRepository videoResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    public List<com.danceflow.dto.VideoCategory> getCategories() {
        List<com.danceflow.entity.VideoCategory> entities = videoCategoryRepository.findAllByOrderByNameAsc();
        return entities.stream()
                .map(e -> new com.danceflow.dto.VideoCategory(
                        e.getId(),
                        e.getName(),
                        e.getDescription(),
                        e.getCoverUrl()
                ))
                .collect(Collectors.toList());
    }

    public VideoListResponse getVideos(int page, int size, Long categoryId, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoResource> videosPage;

        if (search != null && !search.isEmpty()) {
            videosPage = videoResourceRepository.searchVideos(search, pageable);
        } else if (categoryId != null) {
            videosPage = videoResourceRepository.findByCategoryId(categoryId, pageable);
        } else {
            videosPage = videoResourceRepository.findByRecommendedTrueOrderByCreatedAtDesc(pageable);
        }

        Long userId = getCurrentUserId();

        List<VideoResourceResponse> videoResponses = videosPage.getContent().stream()
                .map(video -> convertToVideoResponse(video, userId))
                .collect(Collectors.toList());

        return new VideoListResponse(videoResponses, (int) videosPage.getTotalElements(), page, size);
    }

    public VideoResourceResponse getVideo(Long videoId) {
        VideoResource video = videoResourceRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("视频不存在"));

        // 增加观看数
        video.setViews(video.getViews() + 1);
        video = videoResourceRepository.save(video);

        Long userId = getCurrentUserId();
        return convertToVideoResponse(video, userId);
    }

    @Transactional
    public void favoriteVideo(Long videoId) {
        Long userId = getCurrentUserId();

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndVideoId(userId, videoId);
        if (existing.isPresent()) {
            throw new RuntimeException("已经收藏过了");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setType("video");
        favorite.setVideoId(videoId);
        favoriteRepository.save(favorite);

        // 增加点赞数
        VideoResource video = videoResourceRepository.findById(videoId).orElseThrow();
        video.setLikesCount(video.getLikesCount() + 1);
        videoResourceRepository.save(video);
    }

    @Transactional
    public void unfavoriteVideo(Long videoId) {
        Long userId = getCurrentUserId();

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndVideoId(userId, videoId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());

            // 减少点赞数
            VideoResource video = videoResourceRepository.findById(videoId).orElseThrow();
            video.setLikesCount(Math.max(0, video.getLikesCount() - 1));
            videoResourceRepository.save(video);
        }
    }

    private VideoResourceResponse convertToVideoResponse(VideoResource video, Long currentUserId) {
        com.danceflow.entity.VideoCategory category = video.getCategory();

        com.danceflow.dto.VideoCategory categoryDto = null;
        if (category != null) {
            categoryDto = new com.danceflow.dto.VideoCategory(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.getCoverUrl()
            );
        }

        boolean isFavorited = currentUserId != null &&
                favoriteRepository.findByUserIdAndVideoId(currentUserId, video.getId()).isPresent();

        return new VideoResourceResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getCoverUrl(),
                video.getVideoUrl(),
                categoryDto,
                video.getDuration(),
                video.getViews(),
                video.getLikesCount(),
                isFavorited,
                video.getCreatedAt()
        );
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
