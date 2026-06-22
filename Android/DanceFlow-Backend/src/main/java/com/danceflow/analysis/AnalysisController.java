package com.danceflow.analysis;

import com.danceflow.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/practices")
    public ResponseEntity<?> getPracticeHistory() {
        try {
            PracticeHistoryListResponse response = analysisService.getPracticeHistory();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/practices/{id}")
    public ResponseEntity<?> getPractice(@PathVariable Long id) {
        try {
            PracticeHistoryResponse response = analysisService.getPractice(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/practices")
    public ResponseEntity<?> createPractice(
            @RequestPart("video") MultipartFile video,
            @RequestPart(value = "danceStyle", required = false) String danceStyle
    ) {
        try {
            PracticeHistoryResponse response = analysisService.createPractice(video, danceStyle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/practices/{id}/result")
    public ResponseEntity<?> getAnalysisResult(@PathVariable Long id) {
        try {
            AnalysisResultResponse response = analysisService.getAnalysisResult(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
