package com.lexienglish.dto.mocktest;

import com.lexienglish.entity.TestAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptDto {
    private Long id;
    private Long mockTestId;
    private String mockTestTitle;
    private String testType;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private java.math.BigDecimal totalScore;
    private Integer maxScore;
    private java.math.BigDecimal percentageScore;
    private Integer timeSpentSeconds;
    private java.math.BigDecimal readingScore;
    private java.math.BigDecimal listeningScore;
    private java.math.BigDecimal writingScore;
    private java.math.BigDecimal speakingScore;
    private String feedback;

    public static TestAttemptDto fromEntity(TestAttempt attempt) {
        return TestAttemptDto.builder()
                .id(attempt.getId())
                .mockTestId(attempt.getMockTest().getId())
                .mockTestTitle(attempt.getMockTest().getTitle())
                .testType(attempt.getMockTest().getTestType().name())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .status(attempt.getStatus().name())
                .totalScore(attempt.getTotalScore())
                .maxScore(attempt.getMaxScore())
                .percentageScore(attempt.getPercentageScore())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .readingScore(attempt.getReadingScore())
                .listeningScore(attempt.getListeningScore())
                .writingScore(attempt.getWritingScore())
                .speakingScore(attempt.getSpeakingScore())
                .feedback(attempt.getFeedback())
                .build();
    }
}
