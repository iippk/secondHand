package com.cshp.aiservice2.controller;

import com.cshp.aiservice2.dto.ChatRequest;
import com.cshp.aiservice2.dto.ChatResponse;
import com.cshp.aiservice2.dto.ChatResponse;
import com.cshp.aiservice2.service.AiAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@Validated
public class AiAssistantController {

    @Autowired
    private com.cshp.aiservice2.service.AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest chatRequest,
                             HttpServletRequest request) {
        String studentId = request.getHeader("X-Student-Id");
        log.info("学生 {} 使用AI助手，消息: {}", studentId, chatRequest.getMessage());

        return aiAssistantService.chatWithAI(chatRequest, studentId);
    }

    @PostMapping("/evaluate-price")
    public ChatResponse evaluatePrice(@RequestParam String itemName,
                                      @RequestParam String condition,
                                      @RequestParam String description,
                                      HttpServletRequest request) {
        String studentId = request.getHeader("X-Student-Id");
        log.info("学生 {} 请求评估物品价格: {}, 成色: {}", studentId, itemName, condition);

        return aiAssistantService.evaluateItemPrice(itemName, condition, description, studentId);
    }

    @GetMapping("/health")
    public String health() {
        return "AI Service is running";
    }
}