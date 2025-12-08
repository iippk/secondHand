package com.cshp.aiservice2.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private boolean success;
    private String message;
    private String response;
    private String conversationId;
    private Long timestamp;

    public static ChatResponse success(String response, String conversationId) {
        ChatResponse result = new ChatResponse();
        result.setSuccess(true);
        result.setResponse(response);
        result.setConversationId(conversationId);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static ChatResponse error(String message) {
        ChatResponse result = new ChatResponse();
        result.setSuccess(false);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}