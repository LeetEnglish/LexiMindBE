package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from AI speaking scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingScoringResponse {
    private double overallScore;
    private double maxScore;
    private double percentage;
    private double ieltsBandEquivalent;
    private SpeakingScoreDetail details;
    private String feedback;
    private int wordCount;
    private int processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpeakingScoreDetail {
        private double pronunciationScore;
        private double fluencyScore;
        private double vocabularyScore;
        private double grammarScore;
    }
}
