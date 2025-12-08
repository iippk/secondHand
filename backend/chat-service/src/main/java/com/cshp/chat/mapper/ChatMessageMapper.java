package com.cshp.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cshp.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}

