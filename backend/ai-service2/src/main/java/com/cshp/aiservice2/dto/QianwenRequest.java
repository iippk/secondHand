package com.cshp.aiservice2.dto;

import lombok.Data;
import java.util.List;

@Data
public class QianwenRequest {
    private String model;
    private Input input;
    private Parameters parameters;

    @Data
    public static class Input {
        private List<Message> messages;
    }

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    public static class Parameters {
        private String result_format = "message";
        private Double temperature = 0.7;
        private Integer max_tokens = 2000;
    }
}