package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.B1;

    public enum DifficultyLevel {
        A1, A2, B1, B2, C1, C2
    }
}
