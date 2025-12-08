package com.cshp.aiservice2.dto;

import lombok.Data;
import java.util.List;

@Data
public class QianwenResponse {
    private Output output;
    private String request_id;

    @Data
    public static class Output {
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    public static class Choice {
        private Message message;
        private String finish_reason;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    public static class Usage {
        private Integer total_tokens;
        private Integer input_tokens;
        private Integer output_tokens;
    }
}