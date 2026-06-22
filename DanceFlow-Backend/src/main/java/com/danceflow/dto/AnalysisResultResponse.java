package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponse {
    private Float score;
    private String feedback;
    private List<String> suggestions;
    // 关键帧和身体指标字段预留
    // private List<KeyFrameAnalysis> keyFrames;
    // private BodyMetrics bodyMetrics;
}
