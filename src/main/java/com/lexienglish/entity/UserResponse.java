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
    private java.math.BigDecimal score;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    // For AI-scored responses (Writing/Speaking)
    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "grammar_score")
    private java.math.BigDecimal grammarScore;

    @Column(name = "vocabulary_score")
    private java.math.BigDecimal vocabularyScore;

    @Column(name = "coherence_score")
    private java.math.BigDecimal coherenceScore;

    @Column(name = "task_achievement_score")
    private java.math.BigDecimal taskAchievementScore;

    // For speaking
    @Column(name = "audio_response_url")
    private String audioResponseUrl;

    @Column(name = "pronunciation_score")
    private java.math.BigDecimal pronunciationScore;

    @Column(name = "fluency_score")
    private java.math.BigDecimal fluencyScore;

    /**
     * Grade a multiple choice or simple answer question
     */
    public void gradeSimpleQuestion() {
        if (question == null || userAnswer == null) {
            this.isCorrect = false;
            this.score = java.math.BigDecimal.ZERO;
            return;
        }

        String correct = question.getCorrectAnswer();
        if (correct == null) {
            return; // Needs manual or AI grading
        }

        // Case-insensitive comparison, trim whitespace
        this.isCorrect = correct.trim().equalsIgnoreCase(userAnswer.trim());
        this.score = this.isCorrect ? java.math.BigDecimal.valueOf(question.getPoints()) : java.math.BigDecimal.ZERO;
    }
}
