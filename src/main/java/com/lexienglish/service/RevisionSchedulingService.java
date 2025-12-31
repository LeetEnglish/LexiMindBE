package com.lexienglish.service;

import com.lexienglish.entity.Flashcard;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.repository.FlashcardRepository;
import com.lexienglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Revision Scheduling Service
 * 
 * Manages SRS scheduling and revision sessions for flashcards.
 * Uses SM-2 algorithm implemented in Flashcard entity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevisionSchedulingService {

    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    /**
     * Get flashcards due for review today
     */
    @Transactional(readOnly = true)
    public List<Flashcard> getDueFlashcards(String userEmail, int limit) {
        User user = getUserByEmail(userEmail);
        return flashcardRepository.findDueForReview(user, LocalDateTime.now())
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get daily revision statistics
     */
    @Transactional(readOnly = true)
    public RevisionStats getRevisionStats(String userEmail) {
        User user = getUserByEmail(userEmail);

        long totalCards = flashcardRepository.countByUser(user);
        long dueCards = flashcardRepository.countDueForReview(user, LocalDateTime.now());
        long masteredCards = flashcardRepository.countByUserAndMasteredTrue(user);
        long reviewedToday = flashcardRepository.countReviewedToday(user,
                LocalDateTime.now().toLocalDate().atStartOfDay());

        return RevisionStats.builder()
                .totalCards(totalCards)
                .dueCards(dueCards)
                .masteredCards(masteredCards)
                .reviewedToday(reviewedToday)
                .masteryPercentage(totalCards > 0 ? (masteredCards * 100.0) / totalCards : 0)
                .build();
    }

    /**
     * Process a flashcard review with SM-2 algorithm
     */
    @Transactional
    public Flashcard reviewFlashcard(Long flashcardId, int quality, String userEmail) {
        User user = getUserByEmail(userEmail);

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new BadRequestException("Flashcard not found"));

        if (!flashcard.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Flashcard not found");
        }

        // Apply SM-2 algorithm
        flashcard.updateSRS(quality);

        log.info("Reviewed flashcard {} with quality {}. Next review in {} days",
                flashcardId, quality, flashcard.getInterval());

        return flashcardRepository.save(flashcard);
    }

    /**
     * Get recommended daily review count based on user's progress
     */
    @Transactional(readOnly = true)
    public int getRecommendedDailyReviews(String userEmail) {
        User user = getUserByEmail(userEmail);
        long dueCards = flashcardRepository.countDueForReview(user, LocalDateTime.now());

        // Recommend at least 10, max 50, otherwise due count
        return (int) Math.max(10, Math.min(50, dueCards));
    }

    /**
     * Get weekly revision schedule
     */
    @Transactional(readOnly = true)
    public List<DailySchedule> getWeeklySchedule(String userEmail) {
        User user = getUserByEmail(userEmail);

        return java.util.stream.IntStream.range(0, 7)
                .mapToObj(day -> {
                    LocalDateTime date = LocalDateTime.now().plusDays(day);
                    long count = flashcardRepository.countDueOnDate(user,
                            date.toLocalDate().atStartOfDay(),
                            date.toLocalDate().plusDays(1).atStartOfDay());
                    return new DailySchedule(date.toLocalDate(), count);
                })
                .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    // Inner classes for response
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class RevisionStats {
        private long totalCards;
        private long dueCards;
        private long masteredCards;
        private long reviewedToday;
        private double masteryPercentage;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DailySchedule {
        private java.time.LocalDate date;
        private long cardCount;
    }
}
