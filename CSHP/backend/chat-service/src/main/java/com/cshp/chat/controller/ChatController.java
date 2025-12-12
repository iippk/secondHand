package com.cshp.chat.controller;

import com.cshp.chat.entity.ChatMessage;
import com.cshp.chat.service.ChatService;
import com.cshp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessage message) {
        ChatMessage saved = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/queue/" + message.getReceiverId(), saved);
    }

    @GetMapping("/sessions")
    public Result<List<String>> getSessions(@RequestHeader("X-Student-Id") String userId) {
        List<String> sessions = chatService.getSessionsByUserId(userId);
        return Result.success(sessions);
    }

    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(
            @RequestHeader("X-Student-Id") String userId,
            @RequestParam String sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId, userId);
        return Result.success(messages);
    }

    @PutMapping("/read/{sessionId}")
    public Result<?> markAsRead(@RequestHeader("X-Student-Id") String userId,
                                @PathVariable String sessionId) {
        chatService.markAsRead(sessionId, userId);
        return Result.success("已标记为已读");
    }
}

