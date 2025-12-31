package com.lexienglish.service;

import com.lexienglish.entity.*;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Learning Dashboard Service
 * 
 * Provides centralized dashboard data for user's learning progress.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningDashboardService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final FlashcardRepository flashcardRepository;
    private final LessonRepository lessonRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final RevisionSchedulingService revisionSchedulingService;

    @Transactional(readOnly = true)
    public DashboardData getDashboard(String userEmail) {
        User user = getUserByEmail(userEmail);

        // Documents progress
        long totalDocs = documentRepository.countByUser(user);
        long completedDocs = documentRepository.countByUserAndStatusAndCompletedLessonsEquals(
                user, Document.ProcessingStatus.COMPLETED);

        // Lessons progress
        long totalLessons = lessonRepository.countByDocumentUser(user);
        long completedLessons = lessonRepository.countByDocumentUserAndCompletedTrue(user);

        // Flashcard stats
        RevisionSchedulingService.RevisionStats flashcardStats = revisionSchedulingService.getRevisionStats(userEmail);

        // Test stats
        long totalTests = testAttemptRepository.countByUser(user);
        Double avgScore = testAttemptRepository.findAverageScoreByUser(user);

        // Chat stats
        long chatSessions = chatSessionRepository.countByUserAndStatus(
                user, ChatSession.SessionStatus.ACTIVE);
        long totalMessages = chatSessionRepository.countTotalMessagesByUser(user);

        // Streak calculation (simplified)
        int currentStreak = calculateStreak(user);

        return DashboardData.builder()
                .totalDocuments(totalDocs)
                .documentsProgress(totalDocs > 0 ? (completedDocs * 100.0 / totalDocs) : 0)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .lessonsProgress(totalLessons > 0 ? (completedLessons * 100.0 / totalLessons) : 0)
                .flashcardStats(flashcardStats)
                .totalTestAttempts(totalTests)
                .averageTestScore(avgScore != null ? avgScore : 0)
                .chatSessions(chatSessions)
                .totalMessages(totalMessages)
                .currentStreak(currentStreak)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecommendedAction> getRecommendations(String userEmail) {
        User user = getUserByEmail(userEmail);

        var recommendations = new java.util.ArrayList<RecommendedAction>();

        // Check flashcard dues
        long dueCards = flashcardRepository.countDueForReview(user, java.time.LocalDateTime.now());
        if (dueCards > 0) {
            recommendations.add(new RecommendedAction(
                    "REVIEW_FLASHCARDS",
                    "Review " + dueCards + " flashcard(s) due today",
                    "/self-study/flashcards",
                    "HIGH"));
        }

        // Check incomplete lessons
        long incompleteLessons = totalLessons(user) - completedLessons(user);
        if (incompleteLessons > 0) {
            recommendations.add(new RecommendedAction(
                    "CONTINUE_LEARNING",
                    "Continue your lessons - " + incompleteLessons + " remaining",
                    "/self-study",
                    "MEDIUM"));
        }

        // Suggest practice test
        if (testAttemptRepository.countByUser(user) == 0) {
            recommendations.add(new RecommendedAction(
                    "TAKE_MOCK_TEST",
                    "Take your first mock test to assess your level",
                    "/mock-test",
                    "MEDIUM"));
        }

        return recommendations;
    }

    private int calculateStreak(User user) {
        // Simplified streak: count consecutive days with activity
        // In production, track daily activity in a separate table
        return 0; // Placeholder
    }

    private long totalLessons(User user) {
        return lessonRepository.countByDocumentUser(user);
    }

    private long completedLessons(User user) {
        return lessonRepository.countByDocumentUserAndCompletedTrue(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    // Data classes
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class DashboardData {
        private long totalDocuments;
        private double documentsProgress;
        private long totalLessons;
        private long completedLessons;
        private double lessonsProgress;
        private RevisionSchedulingService.RevisionStats flashcardStats;
        private long totalTestAttempts;
        private double averageTestScore;
        private long chatSessions;
        private long totalMessages;
        private int currentStreak;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RecommendedAction {
        private String type;
        private String message;
        private String actionUrl;
        private String priority;
    }
}
