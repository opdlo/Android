package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private AuthorInfo author;
    private String content;
    private List<String> images;
    private String videoUrl;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLiked;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
