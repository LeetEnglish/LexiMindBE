package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from document parsing via AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseResponse {
    private boolean success;
    private String title;
    private String summary;
    private List<AiLesson> lessons;
    private int totalFlashcards;
    private int processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiLesson {
        private String title;
        private String content;
        private int orderIndex;
        private List<AiFlashcard> flashcards;
        private List<AiExercise> exercises;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiFlashcard {
        private String front;
        private String back;
        private String cardType;
        private String phonetic;
        private String example;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiExercise {
        private String question;
        private String questionType;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
    }
}
