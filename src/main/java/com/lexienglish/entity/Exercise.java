package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise extends BaseEntity {

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @ElementCollection
    @CollectionTable(name = "exercise_options", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "option_text")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    public enum ExerciseType {
        FILL_IN_THE_BLANK,
        MULTIPLE_CHOICE,
        MATCHING,
        TRUE_FALSE
    }
}
