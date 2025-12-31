package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTest extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false)
    private SkillType skillType;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.INTERMEDIATE;

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TestAttempt> attempts = new ArrayList<>();

    public enum TestType {
        IELTS, TOEFL, SAT, ACT, CUSTOM
    }

    public enum SkillType {
        READING, LISTENING, WRITING, SPEAKING, MIXED
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
