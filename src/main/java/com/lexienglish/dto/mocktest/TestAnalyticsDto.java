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
    private java.math.BigDecimal averageScore;
    private java.math.BigDecimal bestScore;
    private long ieltsCompleted;
    private long toeflCompleted;
    private long satCompleted;
    private long actCompleted;

    // Skill-specific analytics
    private java.math.BigDecimal avgReadingScore;
    private java.math.BigDecimal avgListeningScore;
    private java.math.BigDecimal avgWritingScore;
    private java.math.BigDecimal avgSpeakingScore;
}
