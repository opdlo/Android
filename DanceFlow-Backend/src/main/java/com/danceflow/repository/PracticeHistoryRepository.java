package com.danceflow.repository;

import com.danceflow.entity.PracticeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeHistoryRepository extends JpaRepository<PracticeHistory, Long> {
    List<PracticeHistory> findByUserIdOrderByIdDesc(Long userId);
}
