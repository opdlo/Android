package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItem {
    private Long id;
    private String type;
    private Long postId;
    private Long videoId;
    private String title;
    private String coverUrl;
    private LocalDateTime createdAt;
}
