package com.lexienglish.dto.mocktest;

import com.lexienglish.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String content;
    private String questionType;
    private List<String> options;
    private String passage;
    private String audioUrl;
    private String imageUrl;
    private Integer points;
    private Integer orderIndex;
    private Integer timeLimitSeconds;
    // Note: correctAnswer and explanation not included for test-taking

    public static QuestionDto fromEntity(Question question) {
        return QuestionDto.builder()
                .id(question.getId())
                .content(question.getContent())
                .questionType(question.getQuestionType().name())
                .options(question.getOptions())
                .passage(question.getPassage())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .build();
    }

    // Version with answers for review
    public static QuestionDto fromEntityWithAnswers(Question question) {
        QuestionDto dto = fromEntity(question);
        // Could add correctAnswer and explanation for review mode
        return dto;
    }
}
