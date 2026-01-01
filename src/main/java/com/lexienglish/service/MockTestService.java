package com.lexienglish.service;

import com.lexienglish.dto.mocktest.*;
import com.lexienglish.entity.*;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.exception.ResourceNotFoundException;
import com.lexienglish.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockTestService {

    private final MockTestRepository mockTestRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserResponseRepository userResponseRepository;
    private final UserRepository userRepository;
    private final WritingScoringService writingScoringService;
    private final SpeakingScoringService speakingScoringService;

    // ==================== Test Catalog ====================

    @Transactional(readOnly = true)
    public Page<MockTestDto> getAvailableTests(Pageable pageable) {
        return mockTestRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(MockTestDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<MockTestDto> getTestsByFilter(String testType, String skillType, Pageable pageable) {
        MockTest.TestType type = testType != null ? MockTest.TestType.valueOf(testType) : null;
        MockTest.SkillType skill = skillType != null ? MockTest.SkillType.valueOf(skillType) : null;
        return mockTestRepository.findByFilters(type, skill, pageable)
                .map(MockTestDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public MockTestDto getTestDetails(Long testId) {
        MockTest test = mockTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("MockTest", "id", testId));
        return MockTestDto.fromEntity(test);
    }

    // ==================== Taking Tests ====================

    @Transactional
    public TestAttemptDto startTest(Long testId, String userEmail) {
        User user = getUserByEmail(userEmail);
        MockTest test = mockTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("MockTest", "id", testId));

        if (!test.isPublished()) {
            throw new BadRequestException("This test is not available");
        }

        // Check for existing in-progress attempt
        var existingAttempt = testAttemptRepository.findByUserAndMockTestAndStatus(
                user, test, TestAttempt.AttemptStatus.IN_PROGRESS);

        if (existingAttempt.isPresent()) {
            return TestAttemptDto.fromEntity(existingAttempt.get());
        }

        // Create new attempt
        TestAttempt attempt = TestAttempt.builder()
                .user(user)
                .mockTest(test)
                .startedAt(LocalDateTime.now())
                .status(TestAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        attempt = testAttemptRepository.save(attempt);
        log.info("Started test attempt {} for user {} on test {}", attempt.getId(), userEmail, testId);

        return TestAttemptDto.fromEntity(attempt);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getTestQuestions(Long attemptId, String userEmail) {
        TestAttempt attempt = getAttemptForUser(attemptId, userEmail);

        List<Question> questions = questionRepository.findByMockTestOrderByOrderIndexAsc(attempt.getMockTest());
        return questions.stream()
                .map(QuestionDto::fromEntity)
                .toList();
    }

    @Transactional
    public void submitAnswer(Long attemptId, SubmitAnswerRequest request, String userEmail) {
        TestAttempt attempt = getAttemptForUser(attemptId, userEmail);

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Test is not in progress");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", request.getQuestionId()));

        // Check if answer already exists
        var existingResponse = userResponseRepository.findByTestAttemptAndQuestionId(attempt, question.getId());

        UserResponse response;
        if (existingResponse.isPresent()) {
            response = existingResponse.get();
            response.setUserAnswer(request.getAnswer());
        } else {
            response = UserResponse.builder()
                    .testAttempt(attempt)
                    .question(question)
                    .userAnswer(request.getAnswer())
                    .timeSpentSeconds(request.getTimeSpentSeconds())
                    .answeredAt(LocalDateTime.now())
                    .build();
        }

        // Auto-grade simple questions
        if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE ||
                question.getQuestionType() == Question.QuestionType.TRUE_FALSE ||
                question.getQuestionType() == Question.QuestionType.FILL_IN_BLANK) {
            response.gradeSimpleQuestion();
        }

        userResponseRepository.save(response);
    }

    @Transactional
    public TestAttemptDto completeTest(Long attemptId, String userEmail) {
        TestAttempt attempt = getAttemptForUser(attemptId, userEmail);

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Test is not in progress");
        }

        // Calculate time spent
        long seconds = ChronoUnit.SECONDS.between(attempt.getStartedAt(), LocalDateTime.now());
        attempt.setTimeSpentSeconds((int) seconds);
        attempt.setCompletedAt(LocalDateTime.now());

        // AI-grade essay and speaking responses before calculating scores
        gradeAIResponses(attempt);

        // Calculate scores
        attempt.calculateScore();

        // Check if still has ungraded responses (shouldn't happen unless AI fails)
        boolean hasUngraded = attempt.getResponses().stream()
                .anyMatch(r -> r.getScore() == null);

        if (hasUngraded) {
            attempt.setStatus(TestAttempt.AttemptStatus.PENDING_REVIEW);
        } else {
            attempt.setStatus(TestAttempt.AttemptStatus.COMPLETED);
            generateFeedback(attempt);
        }

        attempt = testAttemptRepository.save(attempt);
        log.info("Completed test attempt {} with score {}%", attemptId, attempt.getPercentageScore());

        return TestAttemptDto.fromEntity(attempt);
    }

    /**
     * Grade essay and speaking responses using AI scoring services
     */
    private void gradeAIResponses(TestAttempt attempt) {
        for (UserResponse response : attempt.getResponses()) {
            if (response.getScore() != null) {
                continue; // Already graded
            }

            Question.QuestionType type = response.getQuestion().getQuestionType();

            try {
                if (type == Question.QuestionType.ESSAY || type == Question.QuestionType.SHORT_ANSWER) {
                    log.info("AI grading writing response for question {}", response.getQuestion().getId());
                    writingScoringService.scoreWritingResponse(response);
                    userResponseRepository.save(response);
                } else if (type == Question.QuestionType.SPEAKING) {
                    log.info("AI grading speaking response for question {}", response.getQuestion().getId());
                    speakingScoringService.scoreSpeakingResponse(response);
                    userResponseRepository.save(response);
                }
            } catch (Exception e) {
                log.error("Failed to AI-grade response {}: {}", response.getId(), e.getMessage());
                // Leave as ungraded for manual review
            }
        }
    }

    // ==================== User History & Analytics ====================

    @Transactional(readOnly = true)
    public Page<TestAttemptDto> getUserAttempts(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return testAttemptRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(TestAttemptDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public TestAttemptDto getAttemptResult(Long attemptId, String userEmail) {
        TestAttempt attempt = getAttemptForUser(attemptId, userEmail);
        return TestAttemptDto.fromEntity(attempt);
    }

    @Transactional(readOnly = true)
    public TestAnalyticsDto getUserAnalytics(String userEmail) {
        User user = getUserByEmail(userEmail);

        long total = testAttemptRepository.countByUser(user);
        long completed = testAttemptRepository.countByUserAndStatus(user, TestAttempt.AttemptStatus.COMPLETED);
        Double avgScoreDouble = testAttemptRepository.getAverageScoreByUser(user);
        java.math.BigDecimal avgScore = avgScoreDouble != null ? java.math.BigDecimal.valueOf(avgScoreDouble) : null;

        List<TestAttempt> topScores = testAttemptRepository.findTopScoresByUser(user, PageRequest.of(0, 1));
        java.math.BigDecimal bestScore = topScores.isEmpty() ? null : topScores.get(0).getPercentageScore();

        return TestAnalyticsDto.builder()
                .totalAttempts(total)
                .completedAttempts(completed)
                .averageScore(avgScore)
                .bestScore(bestScore)
                .ieltsCompleted(testAttemptRepository.countCompletedByUserAndTestType(user, MockTest.TestType.IELTS))
                .toeflCompleted(testAttemptRepository.countCompletedByUserAndTestType(user, MockTest.TestType.TOEFL))
                .satCompleted(testAttemptRepository.countCompletedByUserAndTestType(user, MockTest.TestType.SAT))
                .actCompleted(testAttemptRepository.countCompletedByUserAndTestType(user, MockTest.TestType.ACT))
                .build();
    }

    // ==================== Helper Methods ====================

    private TestAttempt getAttemptForUser(Long attemptId, String userEmail) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("TestAttempt", "id", attemptId));

        if (!attempt.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("TestAttempt", "id", attemptId);
        }

        return attempt;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private void generateFeedback(TestAttempt attempt) {
        StringBuilder feedback = new StringBuilder();

        if (attempt.getPercentageScore().compareTo(java.math.BigDecimal.valueOf(80)) >= 0) {
            feedback.append("Excellent performance! ");
        } else if (attempt.getPercentageScore().compareTo(java.math.BigDecimal.valueOf(60)) >= 0) {
            feedback.append("Good job! ");
        } else {
            feedback.append("Keep practicing! ");
        }

        long correct = userResponseRepository.countByTestAttemptAndIsCorrectTrue(attempt);
        long total = userResponseRepository.countByTestAttempt(attempt);

        feedback.append(String.format("You answered %d out of %d questions correctly.", correct, total));

        attempt.setFeedback(feedback.toString());
    }
}
