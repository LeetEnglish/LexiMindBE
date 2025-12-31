package com.lexienglish.repository;

import com.lexienglish.entity.Flashcard;
import com.lexienglish.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

        Page<Flashcard> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

        List<Flashcard> findByUserAndMasteredFalseAndNextReviewDateBeforeOrderByNextReviewDateAsc(
                        User user, LocalDateTime now);

        @Query("SELECT f FROM Flashcard f WHERE f.user = :user AND f.mastered = false " +
                        "AND (f.nextReviewDate IS NULL OR f.nextReviewDate <= :now) " +
                        "ORDER BY f.nextReviewDate ASC NULLS FIRST")
        List<Flashcard> findDueForReview(User user, LocalDateTime now);

        @Query("SELECT f FROM Flashcard f WHERE f.user = :user AND f.mastered = false " +
                        "AND (f.nextReviewDate IS NULL OR f.nextReviewDate <= :now) " +
                        "ORDER BY f.nextReviewDate ASC NULLS FIRST")
        Page<Flashcard> findDueForReview(User user, LocalDateTime now, Pageable pageable);

        long countByUser(User user);

        long countByUserAndMasteredTrue(User user);

        @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.user = :user " +
                        "AND (f.nextReviewDate IS NULL OR f.nextReviewDate <= :now)")
        long countDueForReview(User user, LocalDateTime now);

        @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.user = :user " +
                        "AND f.lastReviewedDate >= :start")
        long countReviewedToday(User user, LocalDateTime start);

        @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.user = :user " +
                        "AND f.nextReviewDate >= :start AND f.nextReviewDate < :end")
        long countDueOnDate(User user, LocalDateTime start, LocalDateTime end);

        List<Flashcard> findByDocumentIdAndUser(Long documentId, User user);
}
