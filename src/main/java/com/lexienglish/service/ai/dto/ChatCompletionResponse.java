package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from AI chat completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionResponse {
    private ChatMessage message;
    private String finishReason;
    private int tokensUsed;
    private int processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
