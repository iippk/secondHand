package com.cshp.aiservice2.dto;


import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ChatRequest {
    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String conversationId;
}