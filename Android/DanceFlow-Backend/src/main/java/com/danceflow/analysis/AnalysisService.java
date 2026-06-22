package com.danceflow.analysis;

import com.danceflow.dto.*;
import com.danceflow.entity.*;
import com.danceflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    @Autowired
    private PracticeHistoryRepository practiceHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PracticeHistoryListResponse getPracticeHistory() {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        List<PracticeHistory> histories = practiceHistoryRepository.findByUserIdOrderByIdDesc(userId);

        List<PracticeHistoryResponse> responses = histories.stream()
                .map(this::convertToPracticeHistoryResponse)
                .toList();

        return new PracticeHistoryListResponse(responses, responses.size());
    }

    public PracticeHistoryResponse getPractice(Long practiceId) {
        PracticeHistory history = practiceHistoryRepository.findById(practiceId)
                .orElseThrow(() -> new RuntimeException("练习记录不存在"));

        // 验证权限
        Long userId = getCurrentUserId();
        if (userId != null && !history.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问");
        }

        return convertToPracticeHistoryResponse(history);
    }

    public PracticeHistoryResponse createPractice(MultipartFile video, String danceStyle) throws IOException {
        Long userId = getCurrentUserId();

        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        // 保存视频文件
        String fileName = UUID.randomUUID().toString() + "_" + video.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, "videos");
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        video.transferTo(filePath.toFile());

        String videoUrl = "/uploads/videos/" + fileName;

        // 创建练习记录
        PracticeHistory history = new PracticeHistory();
        history.setUserId(userId);
        history.setVideoUrl(videoUrl);
        history.setAnalysisStatus("pending"); // 等待分析
        history.setDanceStyle(danceStyle);

        history = practiceHistoryRepository.save(history);

        // TODO: 调用模型分析服务，这里预留接口
        // analyzeVideo(history.getId(), videoUrl);

        return convertToPracticeHistoryResponse(history);
    }

    public AnalysisResultResponse getAnalysisResult(Long practiceId) {
        PracticeHistory history = practiceHistoryRepository.findById(practiceId)
                .orElseThrow(() -> new RuntimeException("练习记录不存在"));

        // TODO: 从模型分析结果返回
        // 目前返回模拟数据
        return new AnalysisResultResponse(
                history.getScore() != null ? history.getScore() : 0f,
                history.getFeedback() != null ? history.getFeedback() : "暂无反馈",
                List.of("建议1", "建议2", "建议3")
        );
    }

    private PracticeHistoryResponse convertToPracticeHistoryResponse(PracticeHistory history) {
        List<String> suggestions = null;
        if (history.getSuggestions() != null) {
            // TODO: 解析 JSON 字符串
        }

        return new PracticeHistoryResponse(
                history.getId(),
                history.getVideoUrl(),
                history.getScore(),
                history.getAnalysisStatus(),
                history.getFeedback(),
               suggestions,
                history.getCreatedAt(),
                history.getDanceStyle()

        );
    }

    // 预留的模型分析接口
    private void analyzeVideo(Long practiceId, String videoUrl) {
        // TODO: 实现模型分析逻辑
        // 1. 调用 Python 模型服务
        // 2. 获取分析结果
        // 3. 更新数据库记录
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
