package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_attempt_id", nullable = false)
    private TestAttempt testAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column
    private Double score;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // For AI-scored responses (Writing/Speaking)
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "grammar_score")
    private Double grammarScore;

    @Column(name = "vocabulary_score")
    private Double vocabularyScore;

    @Column(name = "coherence_score")
    private Double coherenceScore;

    @Column(name = "task_achievement_score")
    private Double taskAchievementScore;

    // For speaking
    @Column(name = "audio_response_url")
    private String audioResponseUrl;

    @Column(name = "pronunciation_score")
    private Double pronunciationScore;

    @Column(name = "fluency_score")
    private Double fluencyScore;

    /**
     * Grade a multiple choice or simple answer question
     */
    public void gradeSimpleQuestion() {
        if (question == null || userAnswer == null) {
            this.isCorrect = false;
            this.score = 0.0;
            return;
        }

        String correct = question.getCorrectAnswer();
        if (correct == null) {
            return; // Needs manual or AI grading
        }

        // Case-insensitive comparison, trim whitespace
        this.isCorrect = correct.trim().equalsIgnoreCase(userAnswer.trim());
        this.score = this.isCorrect ? question.getPoints().doubleValue() : 0.0;
    }
}
