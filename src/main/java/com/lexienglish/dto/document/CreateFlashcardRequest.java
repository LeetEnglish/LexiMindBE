package com.lexienglish.dto.document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlashcardRequest {

    @NotBlank(message = "Front text is required")
    private String front;

    @NotBlank(message = "Back text is required")
    private String back;

    private String example;
    private String phonetic;
    private String cardType; // VOCABULARY, IDIOM, GRAMMAR, PHRASE
    private Long documentId;
}
