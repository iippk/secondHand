package com.cshp.chat.controller;

import com.cshp.chat.dto.ChatSessionDTO;
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
        System.out.println("收到消息: " + message);
        try {
            ChatMessage saved = chatService.saveMessage(message);
            System.out.println("消息已保存: " + saved.getId());
            // 发送给接收者
            messagingTemplate.convertAndSend("/queue/" + message.getReceiverId(), saved);
            System.out.println("消息已发送给接收者: " + message.getReceiverId());
            // 也发送给发送者（用于确认消息已发送）
            messagingTemplate.convertAndSend("/queue/" + message.getSenderId(), saved);
            System.out.println("消息已发送给发送者: " + message.getSenderId());
        } catch (Exception e) {
            System.err.println("处理消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/sessions")
    public Result<List<String>> getSessions(@RequestHeader("X-Student-Id") String userId) {
        List<String> sessions = chatService.getSessionsByUserId(userId);
        return Result.success(sessions);
    }

    @GetMapping("/session-list")
    public Result<List<ChatSessionDTO>> getSessionList(@RequestHeader("X-Student-Id") String userId) {
        List<ChatSessionDTO> sessions = chatService.getSessionList(userId);
        return Result.success(sessions);
    }

    @GetMapping("/messages")
    public Result<List<ChatMessage>> getMessages(
            @RequestHeader("X-Student-Id") String userId,
            @RequestParam String sessionId) {
        List<ChatMessage> messages = chatService.getMessagesBySessionId(sessionId, userId);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestHeader("X-Student-Id") String userId) {
        Integer count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/read/{sessionId}")
    public Result<?> markAsRead(@RequestHeader("X-Student-Id") String userId,
                                @PathVariable String sessionId) {
        chatService.markAsRead(sessionId, userId);
        return Result.success("已标记为已读");
    }
}

