package com.danceflow.repository;

import com.danceflow.entity.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Long> {
    List<VideoCategory> findAllByOrderByNameAsc();
}
