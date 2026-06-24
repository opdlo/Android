package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverUsername;
    private String receiverAvatarUrl;
    private String content;
    private Boolean isRead;
    private String messageType;
    private String mediaUrl;
    private String referenceType;
    private Long referenceId;
    private String referenceContent;
    private LocalDateTime createdAt;
}