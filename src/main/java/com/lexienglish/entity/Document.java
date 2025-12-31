package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;

    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Flashcard> flashcards = new ArrayList<>();

    @Column(name = "total_lessons")
    @Builder.Default
    private Integer totalLessons = 0;

    @Column(name = "completed_lessons")
    @Builder.Default
    private Integer completedLessons = 0;

    public enum FileType {
        PDF, DOCX, TXT
    }

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    public int getProgressPercentage() {
        if (totalLessons == 0)
            return 0;
        return (completedLessons * 100) / totalLessons;
    }
}
