package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeHistoryResponse {
    private Long id;
    private String videoUrl;
    private Float score;
    private String analysisStatus;
    private String feedback;
    private List<String> suggestions;
    private LocalDateTime createdAt;
    private String danceStyle;
}

