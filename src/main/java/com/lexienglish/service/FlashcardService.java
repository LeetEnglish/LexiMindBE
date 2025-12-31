package com.lexienglish.service;

import com.lexienglish.dto.document.CreateFlashcardRequest;
import com.lexienglish.dto.document.FlashcardDto;
import com.lexienglish.entity.Document;
import com.lexienglish.entity.Flashcard;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.exception.ResourceNotFoundException;
import com.lexienglish.repository.DocumentRepository;
import com.lexienglish.repository.FlashcardRepository;
import com.lexienglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Transactional
    public FlashcardDto createFlashcard(CreateFlashcardRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Document document = null;
        if (request.getDocumentId() != null) {
            document = documentRepository.findById(request.getDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document", "id", request.getDocumentId()));
            if (!document.getUser().getEmail().equals(userEmail)) {
                throw new BadRequestException("Document not found");
            }
        }

        Flashcard.CardType cardType = Flashcard.CardType.VOCABULARY;
        if (request.getCardType() != null) {
            try {
                cardType = Flashcard.CardType.valueOf(request.getCardType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default
            }
        }

        Flashcard flashcard = Flashcard.builder()
                .front(request.getFront())
                .back(request.getBack())
                .example(request.getExample())
                .phonetic(request.getPhonetic())
                .cardType(cardType)
                .document(document)
                .user(user)
                .build();

        flashcard = flashcardRepository.save(flashcard);
        log.info("Created flashcard: {} for user: {}", flashcard.getId(), userEmail);

        return FlashcardDto.fromEntity(flashcard);
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> getUserFlashcards(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return flashcardRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(FlashcardDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> getDueFlashcards(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        return flashcardRepository.findDueForReview(user, now).stream()
                .map(FlashcardDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FlashcardDto> getDueFlashcards(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        return flashcardRepository.findDueForReview(user, now, pageable)
                .map(FlashcardDto::fromEntity);
    }

    @Transactional
    public FlashcardDto reviewFlashcard(Long flashcardId, int quality, String userEmail) {
        Flashcard flashcard = getFlashcardForUser(flashcardId, userEmail);

        // Apply SM-2 algorithm
        flashcard.updateSRS(quality);
        flashcard = flashcardRepository.save(flashcard);

        log.info("Reviewed flashcard: {} with quality: {}, next review in {} days",
                flashcardId, quality, flashcard.getInterval());

        return FlashcardDto.fromEntity(flashcard);
    }

    @Transactional(readOnly = true)
    public FlashcardDto getFlashcard(Long id, String userEmail) {
        return FlashcardDto.fromEntity(getFlashcardForUser(id, userEmail));
    }

    @Transactional
    public void deleteFlashcard(Long id, String userEmail) {
        Flashcard flashcard = getFlashcardForUser(id, userEmail);
        flashcardRepository.delete(flashcard);
        log.info("Deleted flashcard: {} for user: {}", id, userEmail);
    }

    @Transactional(readOnly = true)
    public FlashcardStats getStats(String userEmail) {
        User user = getUserByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        long total = flashcardRepository.countByUser(user);
        long mastered = flashcardRepository.countByUserAndMasteredTrue(user);
        long dueForReview = flashcardRepository.countDueForReview(user, now);

        return new FlashcardStats(total, mastered, dueForReview, total - mastered);
    }

    private Flashcard getFlashcardForUser(Long id, String userEmail) {
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard", "id", id));

        if (!flashcard.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Flashcard", "id", id);
        }

        return flashcard;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public record FlashcardStats(long total, long mastered, long dueForReview, long learning) {
    }
}
