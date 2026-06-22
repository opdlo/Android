package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoResourceResponse {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private VideoCategory category;
    private Integer duration;
    private Integer views;
    private Integer likesCount;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
