package com.lexienglish.dto.document;

import com.lexienglish.entity.Flashcard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDto {
    private Long id;
    private String front;
    private String back;
    private String example;
    private String phonetic;
    private String cardType;
    private Long documentId;
    private Double easeFactor;
    private Integer interval;
    private Integer repetitions;
    private LocalDateTime nextReviewDate;
    private boolean mastered;
    private boolean dueForReview;

    public static FlashcardDto fromEntity(Flashcard card) {
        return FlashcardDto.builder()
                .id(card.getId())
                .front(card.getFront())
                .back(card.getBack())
                .example(card.getExample())
                .phonetic(card.getPhonetic())
                .cardType(card.getCardType().name())
                .documentId(card.getDocument() != null ? card.getDocument().getId() : null)
                .easeFactor(card.getEaseFactor())
                .interval(card.getInterval())
                .repetitions(card.getRepetitions())
                .nextReviewDate(card.getNextReviewDate())
                .mastered(card.isMastered())
                .dueForReview(card.isDueForReview())
                .build();
    }
}
