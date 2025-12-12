package com.cshp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.chat.entity.ChatMessage;
import com.cshp.chat.mapper.ChatMessageMapper;
import com.cshp.chat.service.ChatService;
import com.cshp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private String buildSessionId(String senderId, String receiverId) {
        if (senderId == null || receiverId == null) {
            throw new BusinessException("会话信息不完整");
        }
        String[] ids = new String[]{senderId, receiverId};
        Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }
}

