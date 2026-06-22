package com.danceflow.profile;

import com.danceflow.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites() {
        try {
            FavoriteListResponse response = profileService.getFavorites();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file) {
        try {
            String avatarUrl = profileService.uploadAvatar(file);
            return ResponseEntity.ok(avatarUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload-background")
    public ResponseEntity<?> uploadBackground(@RequestPart("file") MultipartFile file) {
        try {
            String url = profileService.uploadBackground(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

