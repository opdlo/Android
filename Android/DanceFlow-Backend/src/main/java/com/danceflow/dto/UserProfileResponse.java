package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String gender;
    private LocalDate birthday;
    private String signature;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private String backgroundUrl;
}

