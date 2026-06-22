package com.danceflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverUrl;

    @Column(nullable = false)
    private String videoUrl;

    @Column(name = "category_id")
    private Long categoryId;

    private Integer duration; // 秒

    private Integer views = 0;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private VideoCategory category;
    private Boolean recommended = false;
}

