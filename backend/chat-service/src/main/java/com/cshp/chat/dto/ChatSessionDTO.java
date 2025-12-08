package com.cshp.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private String sessionId;
    private String otherUserId;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
    private Long productId;
    private String productTitle;
    private String productImage;
    private String productPrice;
}

