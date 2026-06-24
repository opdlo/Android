package com.danceflow.message;

import com.danceflow.dto.ConversationResponse;
import com.danceflow.dto.MessageResponse;
import com.danceflow.dto.SendMessageRequest;
import com.danceflow.entity.Message;
import com.danceflow.entity.User;
import com.danceflow.repository.MessageRepository;
import com.danceflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.danceflow.entity.Post;
import com.danceflow.repository.PostRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        Long senderId = getCurrentUserId();
        if (senderId.equals(request.getReceiverId())) {
            throw new RuntimeException("不能给自己发消息");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setIsRead(false);
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : "text");
        message.setMediaUrl(request.getMediaUrl());
        message.setReferenceType(request.getReferenceType());
        message.setReferenceId(request.getReferenceId());
        messageRepository.save(message);

        User sender = userRepository.findById(senderId).orElse(null);
        User receiver = userRepository.findById(request.getReceiverId()).orElse(null);

        String refContent = null;
        if ("post_share".equals(request.getMessageType()) && request.getReferenceId() != null) {
            refContent = getPostReferenceContent(request.getReferenceId());
        }

        return new MessageResponse(message.getId(), senderId,
                sender != null ? sender.getUsername() : null,
                sender != null ? sender.getAvatarUrl() : null,
                request.getReceiverId(),
                receiver != null ? receiver.getUsername() : null,
                receiver != null ? receiver.getAvatarUrl() : null,
                request.getContent(), false,
                message.getMessageType(), message.getMediaUrl(),
                message.getReferenceType(), message.getReferenceId(), refContent,
                message.getCreatedAt());
    }

    public List<MessageResponse> getConversation(Long otherUserId) {
        Long currentUserId = getCurrentUserId();
        List<Message> messages = messageRepository.findConversation(currentUserId, otherUserId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<ConversationResponse> getConversations() {
        Long currentUserId = getCurrentUserId();
        List<Message> allMessages = messageRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(currentUserId, currentUserId);
        Map<Long, ConversationResponse> conversationMap = new LinkedHashMap<>();
        Set<Long> processedUsers = new HashSet<>();

        for (Message msg : allMessages) {
            Long otherId = msg.getSenderId().equals(currentUserId) ? msg.getReceiverId() : msg.getSenderId();
            if (!processedUsers.contains(otherId)) {
                processedUsers.add(otherId);
                User other = userRepository.findById(otherId).orElse(null);
                conversationMap.put(otherId, new ConversationResponse(
                        otherId,
                        other != null ? other.getUsername() : null,
                        other != null ? other.getAvatarUrl() : null,
                        msg.getContent(),
                        msg.getCreatedAt(),
                        msg.getReceiverId().equals(currentUserId) && !msg.getIsRead() ? 1L : 0L,
                        msg.getMessageType(),
                        msg.getMediaUrl()
                ));
            }
        }

        long totalUnread = messageRepository.countByReceiverIdAndIsReadFalse(currentUserId);
        for (ConversationResponse conv : conversationMap.values()) {
            conv.setUnreadCount(totalUnread);
        }

        return new ArrayList<>(conversationMap.values());
    }

    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Transactional
    public void markAllAsRead(Long senderId) {
        Long currentUserId = getCurrentUserId();
        List<Message> unreadMessages = messageRepository.findConversation(senderId, currentUserId);
        for (Message m : unreadMessages) {
            if (m.getReceiverId().equals(currentUserId) && !m.getIsRead()) {
                m.setIsRead(true);
                messageRepository.save(m);
            }
        }
    }

    public long getUnreadCount() {
        Long currentUserId = getCurrentUserId();
        return messageRepository.countByReceiverIdAndIsReadFalse(currentUserId);
    }

    private MessageResponse convertToResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
        String refContent = null;
        if ("post_share".equals(message.getMessageType()) && message.getReferenceId() != null) {
            refContent = getPostReferenceContent(message.getReferenceId());
        }
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                sender != null ? sender.getUsername() : null,
                sender != null ? sender.getAvatarUrl() : null,
                message.getReceiverId(),
                receiver != null ? receiver.getUsername() : null,
                receiver != null ? receiver.getAvatarUrl() : null,
                message.getContent(),
                message.getIsRead(),
                message.getMessageType(),
                message.getMediaUrl(),
                message.getReferenceType(),
                message.getReferenceId(),
                refContent,
                message.getCreatedAt()
        );
    }

    private String getPostReferenceContent(Long postId) {
        try {
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                String text = post.getContent();
                if (text != null && text.length() > 50) text = text.substring(0, 50) + "...";
                return text;
            }
        } catch (Exception e) {}
        return null;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}