package com.lexienglish.service;

import com.lexienglish.entity.Flashcard;
import com.lexienglish.entity.User;
import com.lexienglish.repository.FlashcardRepository;
import com.lexienglish.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisionSchedulingServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RevisionSchedulingService revisionService;

    private User testUser;
    private Flashcard testFlashcard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testFlashcard = Flashcard.builder()
                .id(1L)
                .front("Hello")
                .back("Xin chào")
                .user(testUser)
                .easeFactor(2.5)
                .interval(0)
                .repetitions(0)
                .build();
    }

    @Test
    void getDueFlashcards_ReturnsCards() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(flashcardRepository.findDueForReview(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(List.of(testFlashcard));

        // When
        List<Flashcard> result = revisionService.getDueFlashcards("test@example.com", 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFront()).isEqualTo("Hello");
    }

    @Test
    void getRevisionStats_ReturnsStats() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(flashcardRepository.countByUser(testUser)).thenReturn(100L);
        when(flashcardRepository.countDueForReview(eq(testUser), any())).thenReturn(10L);
        when(flashcardRepository.countByUserAndMasteredTrue(testUser)).thenReturn(50L);
        when(flashcardRepository.countReviewedToday(eq(testUser), any())).thenReturn(5L);

        // When
        var stats = revisionService.getRevisionStats("test@example.com");

        // Then
        assertThat(stats.getTotalCards()).isEqualTo(100L);
        assertThat(stats.getDueCards()).isEqualTo(10L);
        assertThat(stats.getMasteredCards()).isEqualTo(50L);
        assertThat(stats.getMasteryPercentage()).isEqualTo(50.0);
    }

    @Test
    void reviewFlashcard_UpdatesSRS() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(testFlashcard));
        when(flashcardRepository.save(any(Flashcard.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Flashcard result = revisionService.reviewFlashcard(1L, 4, "test@example.com");

        // Then
        assertThat(result.getRepetitions()).isEqualTo(1);
        assertThat(result.getNextReviewDate()).isNotNull();
        verify(flashcardRepository).save(any(Flashcard.class));
    }

    @Test
    void getRecommendedDailyReviews_ReturnsMinimum10() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(flashcardRepository.countDueForReview(eq(testUser), any())).thenReturn(5L);

        // When
        int result = revisionService.getRecommendedDailyReviews("test@example.com");

        // Then
        assertThat(result).isEqualTo(10); // Minimum is 10
    }

    @Test
    void getWeeklySchedule_Returns7Days() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(flashcardRepository.countDueOnDate(eq(testUser), any(), any())).thenReturn(5L);

        // When
        var schedule = revisionService.getWeeklySchedule("test@example.com");

        // Then
        assertThat(schedule).hasSize(7);
    }
}
