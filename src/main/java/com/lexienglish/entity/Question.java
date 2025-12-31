package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "passage", columnDefinition = "TEXT")
    private String passage; // For reading comprehension

    @Column(name = "audio_url")
    private String audioUrl; // For listening questions

    @Column(name = "image_url")
    private String imageUrl; // For visual questions

    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    public enum QuestionType {
        MULTIPLE_CHOICE,
        FILL_IN_BLANK,
        TRUE_FALSE,
        SHORT_ANSWER,
        ESSAY,
        SPEAKING,
        MATCHING
    }
}
