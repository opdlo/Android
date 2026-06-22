package com.danceflow.repository;

import com.danceflow.entity.VideoResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoResourceRepository extends JpaRepository<VideoResource, Long> {
    Page<VideoResource> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT v FROM VideoResource v WHERE v.title LIKE %:search% ORDER BY v.createdAt DESC")
    Page<VideoResource> searchVideos(@Param("search") String search, Pageable pageable);

    Page<VideoResource> findAllByOrderByIdDesc(Pageable pageable);

    Page<VideoResource> findByRecommendedTrueOrderByCreatedAtDesc(Pageable pageable);
}

