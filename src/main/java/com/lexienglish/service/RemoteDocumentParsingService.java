package com.lexienglish.service;

import com.lexienglish.entity.*;
import com.lexienglish.repository.DocumentRepository;
import com.lexienglish.repository.FlashcardRepository;
import com.lexienglish.repository.LessonRepository;
import com.lexienglish.service.ai.RemoteAiService;
import com.lexienglish.service.ai.dto.DocumentParseRequest;
import com.lexienglish.service.ai.dto.DocumentParseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Remote implementation of DocumentParsingService.
 * Uses RemoteAiService to call Python AI microservice for document parsing.
 * 
 * Activated when ai-service.enabled=true in application.yml
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-service.enabled", havingValue = "true")
public class RemoteDocumentParsingService implements DocumentParsingService {

    private final DocumentRepository documentRepository;
    private final LessonRepository lessonRepository;
    private final FlashcardRepository flashcardRepository;
    private final FileStorageService fileStorageService;
    private final RemoteAiService remoteAiService;

    @Override
    @Transactional
    public void parseDocument(Long documentId) {
        log.info("Starting remote AI parsing for document: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        try {
            // Update status to processing
            document.setStatus(Document.ProcessingStatus.PROCESSING);
            documentRepository.save(document);

            // Read and encode file content
            byte[] fileBytes = fileStorageService.readFileBytes(document.getFilePath());
            String encodedContent = Base64.getEncoder().encodeToString(fileBytes);

            // Build request for AI service
            DocumentParseRequest request = DocumentParseRequest.builder()
                    .content(encodedContent)
                    .documentType(document.getFileType().name().toLowerCase())
                    .filename(document.getFileName())
                    .generateLessons(true)
                    .generateFlashcards(true)
                    .build();

            // Call AI service
            DocumentParseResponse response = remoteAiService.parseDocument(request);

            if (response.isSuccess()) {
                // Convert AI response to entities
                List<Lesson> lessons = convertLessons(document, response);
                lessonRepository.saveAll(lessons);

                List<Flashcard> flashcards = convertFlashcards(document, response);
                flashcardRepository.saveAll(flashcards);

                // Update document
                document.setTotalLessons(lessons.size());
                document.setStatus(Document.ProcessingStatus.COMPLETED);
                documentRepository.save(document);

                log.info("AI parsing completed for document: {}. Lessons: {}, Flashcards: {}",
                        documentId, lessons.size(), flashcards.size());
            } else {
                throw new RuntimeException("AI service returned unsuccessful response");
            }

        } catch (Exception e) {
            log.error("AI parsing failed for document: {}", documentId, e);
            document.setStatus(Document.ProcessingStatus.FAILED);
            documentRepository.save(document);
        }
    }

    /**
     * Convert AI response lessons to entity Lessons
     */
    private List<Lesson> convertLessons(Document document, DocumentParseResponse response) {
        List<Lesson> lessons = new ArrayList<>();

        if (response.getLessons() == null) {
            return lessons;
        }

        for (DocumentParseResponse.AiLesson aiLesson : response.getLessons()) {
            Lesson lesson = Lesson.builder()
                    .title(aiLesson.getTitle())
                    .content(aiLesson.getContent())
                    .orderIndex(aiLesson.getOrderIndex())
                    .document(document)
                    .difficultyLevel(Lesson.DifficultyLevel.B1) // Default, can be enhanced
                    .build();

            // Convert exercises
            if (aiLesson.getExercises() != null) {
                List<Exercise> exercises = new ArrayList<>();
                for (DocumentParseResponse.AiExercise aiEx : aiLesson.getExercises()) {
                    Exercise exercise = Exercise.builder()
                            .question(aiEx.getQuestion())
                            .exerciseType(mapExerciseType(aiEx.getQuestionType()))
                            .correctAnswer(aiEx.getCorrectAnswer())
                            .options(aiEx.getOptions())
                            .explanation(aiEx.getExplanation())
                            .lesson(lesson)
                            .build();
                    exercises.add(exercise);
                }
                lesson.setExercises(exercises);
            }

            lessons.add(lesson);
        }

        return lessons;
    }

    /**
     * Convert AI response flashcards to entity Flashcards
     */
    private List<Flashcard> convertFlashcards(Document document, DocumentParseResponse response) {
        List<Flashcard> flashcards = new ArrayList<>();

        if (response.getLessons() == null) {
            return flashcards;
        }

        for (DocumentParseResponse.AiLesson aiLesson : response.getLessons()) {
            if (aiLesson.getFlashcards() == null)
                continue;

            for (DocumentParseResponse.AiFlashcard aiCard : aiLesson.getFlashcards()) {
                Flashcard card = Flashcard.builder()
                        .front(aiCard.getFront())
                        .back(aiCard.getBack())
                        .example(aiCard.getExample())
                        .phonetic(aiCard.getPhonetic())
                        .cardType(mapCardType(aiCard.getCardType()))
                        .document(document)
                        .user(document.getUser())
                        .build();
                flashcards.add(card);
            }
        }

        return flashcards;
    }

    private Exercise.ExerciseType mapExerciseType(String type) {
        if (type == null)
            return Exercise.ExerciseType.MULTIPLE_CHOICE;
        return switch (type.toUpperCase()) {
            case "FILL_IN_BLANK", "FILL_IN_THE_BLANK" -> Exercise.ExerciseType.FILL_IN_THE_BLANK;
            case "TRUE_FALSE" -> Exercise.ExerciseType.TRUE_FALSE;
            case "MATCHING" -> Exercise.ExerciseType.MATCHING;
            case "SHORT_ANSWER" -> Exercise.ExerciseType.SHORT_ANSWER;
            default -> Exercise.ExerciseType.MULTIPLE_CHOICE;
        };
    }

    private Flashcard.CardType mapCardType(String type) {
        if (type == null)
            return Flashcard.CardType.VOCABULARY;
        return switch (type.toUpperCase()) {
            case "PHRASE" -> Flashcard.CardType.PHRASE;
            case "GRAMMAR" -> Flashcard.CardType.GRAMMAR;
            case "IDIOM" -> Flashcard.CardType.IDIOM;
            default -> Flashcard.CardType.VOCABULARY;
        };
    }
}
