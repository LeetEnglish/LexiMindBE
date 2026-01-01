package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(name = "phonetic")
    private String phonetic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardType cardType = CardType.VOCABULARY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SRS (Spaced Repetition System) fields - SM-2 algorithm
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private java.math.BigDecimal easeFactor = new java.math.BigDecimal("2.5");

    @Column(nullable = false)
    @Builder.Default
    private Integer interval = 0; // Days until next review

    @Column(nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "last_reviewed_date")
    private LocalDateTime lastReviewedDate;

    @Column(name = "is_mastered", nullable = false)
    @Builder.Default
    private boolean mastered = false;

    public enum CardType {
        VOCABULARY, IDIOM, GRAMMAR, PHRASE
    }

    /**
     * SM-2 Algorithm implementation for spaced repetition
     * 
     * @param quality User's self-rating 0-5 (0=complete blackout, 5=perfect
     *                response)
     */
    public void updateSRS(int quality) {
        if (quality < 0)
            quality = 0;
        if (quality > 5)
            quality = 5;

        // Update repetitions count
        if (quality >= 3) {
            repetitions++;
        } else {
            repetitions = 0;
        }

        // Update ease factor
        double currentEase = easeFactor.doubleValue();
        double newEase = Math.max(1.3, currentEase + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
        easeFactor = java.math.BigDecimal.valueOf(newEase);

        // Calculate interval
        if (repetitions == 0) {
            interval = 1;
        } else if (repetitions == 1) {
            interval = 1;
        } else if (repetitions == 2) {
            interval = 6;
        } else {
            interval = (int) Math.round(interval * easeFactor.doubleValue());
        }

        // Set next review date
        lastReviewedDate = LocalDateTime.now();
        nextReviewDate = LocalDateTime.now().plusDays(interval);

        // Mark as mastered if interval > 21 days and quality >= 4
        if (interval > 21 && quality >= 4) {
            mastered = true;
        }
    }

    public boolean isDueForReview() {
        if (nextReviewDate == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(nextReviewDate);
    }
}
