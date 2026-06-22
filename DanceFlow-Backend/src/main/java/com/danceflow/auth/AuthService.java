package com.danceflow.auth;

import com.danceflow.dto.*;
import com.danceflow.entity.User;
import com.danceflow.repository.UserRepository;
import com.danceflow.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public UserProfileResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getGender(),
                user.getBirthday(),
                user.getSignature(),
               user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getBackgroundUrl()

        );
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ISO_DATE));
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
       if (request.getAvatarUrl() != null) {
           user.setAvatarUrl(request.getAvatarUrl());
       }
        if (request.getBackgroundUrl() != null) {
            user.setBackgroundUrl(request.getBackgroundUrl());
        }

        user = userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getGender(),
                user.getBirthday(),
                user.getSignature(),
               user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getBackgroundUrl()
        );
   }
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
