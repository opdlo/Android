package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long receiverId;
    private String content;
    private String messageType = "text";
    private String mediaUrl;
    private String referenceType;
    private Long referenceId;
}