package com.cshp.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String content;
    private Integer type; // 0:文本 1:图片
    private Integer readStatus; // 0:未读 1:已读
    private LocalDateTime createTime;
}

