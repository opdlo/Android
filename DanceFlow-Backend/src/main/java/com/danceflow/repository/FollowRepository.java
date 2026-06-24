package com.danceflow.repository;

import com.danceflow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    List<Follow> findByFollowerId(Long followerId);
    List<Follow> findByFolloweeId(Long followeeId);
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    long countByFollowerId(Long followerId);
    long countByFolloweeId(Long followeeId);
}