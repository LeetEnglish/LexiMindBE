package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for AI chat completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionRequest {
    private List<ChatMessage> messages;
    @Builder.Default
    private int maxTokens = 256;
    @Builder.Default
    private double temperature = 0.7;
    private String systemPrompt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role; // user, assistant, system
        private String content;
    }
}
