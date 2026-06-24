package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private Boolean isMutual;
}