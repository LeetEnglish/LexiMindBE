package com.lexienglish.dto.mocktest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAnalyticsDto {
    private long totalAttempts;
    private long completedAttempts;
    private Double averageScore;
    private Double bestScore;
    private long ieltsCompleted;
    private long toeflCompleted;
    private long satCompleted;
    private long actCompleted;

    // Skill-specific analytics
    private Double avgReadingScore;
    private Double avgListeningScore;
    private Double avgWritingScore;
    private Double avgSpeakingScore;
}
