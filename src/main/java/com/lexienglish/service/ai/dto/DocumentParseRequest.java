package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to parse a document via AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseRequest {
    private String content; // Base64 encoded or raw text
    private String documentType; // pdf, docx, txt
    private String filename;
    @Builder.Default
    private boolean generateLessons = true;
    @Builder.Default
    private boolean generateFlashcards = true;
}
