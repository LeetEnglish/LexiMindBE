package com.lexienglish.dto.chat;

import com.lexienglish.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String role;
    private String content;
    private Integer tokensUsed;
    private Integer processingTimeMs;
    private String grammarIssues;
    private String vocabularySuggestions;
    private String correctedText;
    private Double improvementScore;
    private LocalDateTime createdAt;

    public static ChatMessageDto fromEntity(ChatMessage entity) {
        return ChatMessageDto.builder()
                .id(entity.getId())
                .role(entity.getRole().name())
                .content(entity.getContent())
                .tokensUsed(entity.getTokensUsed())
                .processingTimeMs(entity.getProcessingTimeMs())
                .grammarIssues(entity.getGrammarIssues())
                .vocabularySuggestions(entity.getVocabularySuggestions())
                .correctedText(entity.getCorrectedText())
                .improvementScore(entity.getImprovementScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
