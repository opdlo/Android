package com.danceflow.repository;

import com.danceflow.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserIdOrderByIdDesc(Long userId);

    Optional<Favorite> findByUserIdAndPostId(Long userId, Long postId);

    Optional<Favorite> findByUserIdAndVideoId(Long userId, Long videoId);
}
