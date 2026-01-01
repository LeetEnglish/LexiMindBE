package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "total_score")
    private java.math.BigDecimal totalScore;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "percentage_score")
    private java.math.BigDecimal percentageScore;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @OneToMany(mappedBy = "testAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserResponse> responses = new ArrayList<>();

    // Skill-specific scores for IELTS-style tests
    @Column(name = "reading_score")
    private java.math.BigDecimal readingScore;

    @Column(name = "listening_score")
    private java.math.BigDecimal listeningScore;

    @Column(name = "writing_score")
    private java.math.BigDecimal writingScore;

    @Column(name = "speaking_score")
    private java.math.BigDecimal speakingScore;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED, ABANDONED, PENDING_REVIEW
    }

    public void calculateScore() {
        if (responses == null || responses.isEmpty()) {
            this.totalScore = java.math.BigDecimal.ZERO;
            this.percentageScore = java.math.BigDecimal.ZERO;
            return;
        }

        java.math.BigDecimal earned = responses.stream()
                .filter(r -> r.getScore() != null)
                .map(UserResponse::getScore)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        int max = responses.stream()
                .mapToInt(r -> r.getQuestion().getPoints())
                .sum();

        this.totalScore = earned;
        this.maxScore = max;
        this.percentageScore = max > 0 ? earned.multiply(java.math.BigDecimal.valueOf(100)).divide(java.math.BigDecimal.valueOf(max), 2, java.math.RoundingMode.HALF_UP) : java.math.BigDecimal.ZERO;
    }
}
