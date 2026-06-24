package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private String lastMessageType;
    private String lastMessageMediaUrl;
}