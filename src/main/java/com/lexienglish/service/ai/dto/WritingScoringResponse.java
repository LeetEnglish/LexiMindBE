package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from AI writing scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingScoringResponse {
    private double overallScore;
    private double maxScore;
    private double percentage;
    private WritingScoreDetail details;
    private String feedback;
    private int wordCount;
    private int processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WritingScoreDetail {
        private double grammarScore;
        private double vocabularyScore;
        private double coherenceScore;
        private double taskAchievementScore;
    }
}
