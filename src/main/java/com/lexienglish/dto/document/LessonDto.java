package com.lexienglish.dto.document;

import com.lexienglish.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Integer orderIndex;
    private boolean completed;
    private String difficultyLevel;
    private Long documentId;
    private Integer exerciseCount;

    public static LessonDto fromEntity(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .summary(lesson.getSummary())
                .content(lesson.getContent())
                .orderIndex(lesson.getOrderIndex())
                .completed(lesson.isCompleted())
                .difficultyLevel(lesson.getDifficultyLevel().name())
                .documentId(lesson.getDocument().getId())
                .exerciseCount(lesson.getExercises() != null ? lesson.getExercises().size() : 0)
                .build();
    }
}
