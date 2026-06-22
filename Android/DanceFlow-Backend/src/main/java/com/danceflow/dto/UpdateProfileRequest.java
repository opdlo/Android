package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String gender;
    private String birthday;
    private String signature;
    private String avatarUrl;
    private String backgroundUrl;
}

