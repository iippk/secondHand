package com.cshp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.chat.dto.ChatSessionDTO;
import com.cshp.chat.entity.ChatMessage;
import com.cshp.chat.mapper.ChatMessageMapper;
import com.cshp.chat.service.ChatService;
import com.cshp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if (!StringUtils.hasText(message.getSessionId())) {
            message.setSessionId(buildSessionId(message.getSenderId(), message.getReceiverId()));
        }
        message.setCreateTime(LocalDateTime.now());
        message.setReadStatus(0);
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(String sessionId, String userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.and(w -> w.eq("sender_id", userId).or().eq("receiver_id", userId));
        wrapper.orderByAsc("create_time");
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    public List<String> getSessionsByUserId(String userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("sender_id", userId).or().eq("receiver_id", userId));
        wrapper.orderByDesc("create_time");
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        
        Set<String> sessions = new HashSet<>();
        for (ChatMessage message : messages) {
            if (message.getSenderId().equals(userId)) {
                sessions.add(message.getReceiverId());
            } else {
                sessions.add(message.getSenderId());
            }
        }
        return sessions.stream().collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String sessionId, String userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.eq("receiver_id", userId);
        wrapper.eq("read_status", 0);
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        for (ChatMessage message : messages) {
            message.setReadStatus(1);
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    public List<ChatSessionDTO> getSessionList(String userId) {
        // 获取所有与该用户相关的消息
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("sender_id", userId).or().eq("receiver_id", userId));
        wrapper.orderByDesc("create_time");
        List<ChatMessage> allMessages = chatMessageMapper.selectList(wrapper);
        
        // 按sessionId分组，获取每个会话的最后一条消息
        Map<String, ChatMessage> sessionLastMessageMap = new HashMap<>();
        Map<String, String> sessionOtherUserMap = new HashMap<>();
        Map<String, Integer> sessionUnreadMap = new HashMap<>();
        
        for (ChatMessage message : allMessages) {
            String sessionId = message.getSessionId();
            String otherUserId = message.getSenderId().equals(userId) 
                ? message.getReceiverId() 
                : message.getSenderId();
            
            // 更新最后一条消息
            if (!sessionLastMessageMap.containsKey(sessionId) || 
                message.getCreateTime().isAfter(sessionLastMessageMap.get(sessionId).getCreateTime())) {
                sessionLastMessageMap.put(sessionId, message);
                sessionOtherUserMap.put(sessionId, otherUserId);
            }
            
            // 统计未读消息数
            if (message.getReceiverId().equals(userId) && message.getReadStatus() == 0) {
                sessionUnreadMap.put(sessionId, sessionUnreadMap.getOrDefault(sessionId, 0) + 1);
            }
        }
        
        // 转换为DTO列表
        List<ChatSessionDTO> sessionList = new ArrayList<>();
        for (Map.Entry<String, ChatMessage> entry : sessionLastMessageMap.entrySet()) {
            String sessionId = entry.getKey();
            ChatMessage lastMessage = entry.getValue();
            
            ChatSessionDTO dto = new ChatSessionDTO();
            dto.setSessionId(sessionId);
            dto.setOtherUserId(sessionOtherUserMap.get(sessionId));
            dto.setOtherUserName(lastMessage.getSenderId().equals(userId) 
                ? lastMessage.getReceiverName() 
                : lastMessage.getSenderName());
            dto.setLastMessage(lastMessage.getContent());
            dto.setLastTime(lastMessage.getCreateTime());
            dto.setUnreadCount(sessionUnreadMap.getOrDefault(sessionId, 0));
            dto.setProductId(lastMessage.getProductId());
            dto.setProductTitle(lastMessage.getProductTitle());
            dto.setProductImage(lastMessage.getProductImage());
            
            sessionList.add(dto);
        }
        
        // 按最后消息时间排序
        sessionList.sort((a, b) -> b.getLastTime().compareTo(a.getLastTime()));
        
        return sessionList;
    }

    @Override
    public Integer getUnreadCount(String userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId);
        wrapper.eq("read_status", 0);
        return Math.toIntExact(chatMessageMapper.selectCount(wrapper));
    }

    private String buildSessionId(String senderId, String receiverId) {
        if (senderId == null || receiverId == null) {
            throw new BusinessException("会话信息不完整");
        }
        String[] ids = new String[]{senderId, receiverId};
        Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }
}

