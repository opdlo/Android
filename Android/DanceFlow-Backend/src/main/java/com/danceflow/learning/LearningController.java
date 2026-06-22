package com.danceflow.learning;

import com.danceflow.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    @Autowired
    private LearningService learningService;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            return ResponseEntity.ok(learningService.getCategories());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/videos")
    public ResponseEntity<?> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search
    ) {
        try {
            VideoListResponse response = learningService.getVideos(page, size, category, search);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/videos/{id}")
    public ResponseEntity<?> getVideo(@PathVariable Long id) {
        try {
            VideoResourceResponse response = learningService.getVideo(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/videos/{id}/favorite")
    public ResponseEntity<?> favoriteVideo(@PathVariable Long id) {
        try {
            learningService.favoriteVideo(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/videos/{id}/favorite")
    public ResponseEntity<?> unfavoriteVideo(@PathVariable Long id) {
        try {
            learningService.unfavoriteVideo(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
