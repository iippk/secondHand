package com.cshp.chat.service;

import com.cshp.chat.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesBySessionId(String sessionId, String userId);
    List<String> getSessionsByUserId(String userId);
    void markAsRead(String sessionId, String userId);
}

