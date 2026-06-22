package com.danceflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String videoUrl;

    private Float score;

    @Column(name = "analysis_status")
    private String analysisStatus = "pending"; // pending, analyzing, completed, failed

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "JSON")
    private String suggestions;

    @Column(name = "dance_style")
    private String danceStyle;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
