package com.lexienglish.service;

import com.lexienglish.entity.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI Writing Scoring Service
 * 
 * Evaluates writing responses on multiple dimensions:
 * - Grammar & Mechanics
 * - Vocabulary & Word Choice
 * - Coherence & Cohesion
 * - Task Achievement
 * 
 * TODO: Replace mock scoring with actual AI integration (e.g., Hugging Face
 * API)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingScoringService {

    // Scoring criteria weights
    private static final double GRAMMAR_WEIGHT = 0.25;
    private static final double VOCABULARY_WEIGHT = 0.25;
    private static final double COHERENCE_WEIGHT = 0.25;
    private static final double TASK_ACHIEVEMENT_WEIGHT = 0.25;

    /**
     * Score a writing response
     * 
     * @param response The user's response containing the essay
     * @return The updated response with scores and feedback
     */
    public UserResponse scoreWritingResponse(UserResponse response) {
        String essay = response.getUserAnswer();

        if (essay == null || essay.trim().isEmpty()) {
            response.setScore(0.0);
            response.setAiFeedback("No response provided.");
            return response;
        }

        log.info("Scoring writing response for question: {}", response.getQuestion().getId());

        // Calculate individual scores (mock implementation - uses heuristics)
        double grammarScore = evaluateGrammar(essay);
        double vocabularyScore = evaluateVocabulary(essay);
        double coherenceScore = evaluateCoherence(essay);
        double taskAchievementScore = evaluateTaskAchievement(essay, response.getQuestion().getContent());

        // Set component scores
        response.setGrammarScore(grammarScore);
        response.setVocabularyScore(vocabularyScore);
        response.setCoherenceScore(coherenceScore);
        response.setTaskAchievementScore(taskAchievementScore);

        // Calculate overall score (0-10 scale)
        double overallScore = (grammarScore * GRAMMAR_WEIGHT +
                vocabularyScore * VOCABULARY_WEIGHT +
                coherenceScore * COHERENCE_WEIGHT +
                taskAchievementScore * TASK_ACHIEVEMENT_WEIGHT);

        // Convert to points based on question points value
        double maxPoints = response.getQuestion().getPoints().doubleValue();
        response.setScore((overallScore / 10.0) * maxPoints);

        // Generate feedback
        response.setAiFeedback(generateFeedback(grammarScore, vocabularyScore,
                coherenceScore, taskAchievementScore, essay));

        response.setIsCorrect(overallScore >= 6.0); // Passing threshold

        log.info("Writing score: {} / {} (overall: {})", response.getScore(), maxPoints, overallScore);

        return response;
    }

    /**
     * Evaluate grammar and mechanics
     * Mock implementation - TODO: Replace with AI
     */
    private double evaluateGrammar(String essay) {
        int wordCount = essay.split("\\s+").length;
        int sentenceCount = essay.split("[.!?]+").length;

        // Basic heuristics
        double score = 6.0; // Base score

        // Check sentence length variety
        double avgSentenceLength = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0;
        if (avgSentenceLength >= 10 && avgSentenceLength <= 25) {
            score += 1.5;
        }

        // Check for common issues (simplified)
        if (!essay.contains("  "))
            score += 0.5; // No double spaces
        if (Character.isUpperCase(essay.charAt(0)))
            score += 0.5; // Starts with capital
        if (essay.matches(".*[.!?]$"))
            score += 0.5; // Ends with punctuation

        // Check for paragraph structure
        if (essay.contains("\n\n") || essay.length() > 500)
            score += 1.0;

        return Math.min(10.0, score);
    }

    /**
     * Evaluate vocabulary and word choice
     * Mock implementation - TODO: Replace with AI
     */
    private double evaluateVocabulary(String essay) {
        String[] words = essay.toLowerCase().split("\\s+");
        int wordCount = words.length;

        // Count unique words for variety
        long uniqueWords = java.util.Arrays.stream(words).distinct().count();
        double lexicalDiversity = wordCount > 0 ? (double) uniqueWords / wordCount : 0;

        double score = 5.0; // Base score

        // Higher diversity = better vocabulary
        score += lexicalDiversity * 4.0;

        // Check for advanced vocabulary (simplified)
        String[] advancedWords = { "furthermore", "moreover", "consequently", "nevertheless",
                "subsequently", "therefore", "however", "significant",
                "demonstrate", "illustrate", "analyze", "evaluate" };
        for (String adv : advancedWords) {
            if (essay.toLowerCase().contains(adv)) {
                score += 0.3;
            }
        }

        return Math.min(10.0, score);
    }

    /**
     * Evaluate coherence and cohesion
     * Mock implementation - TODO: Replace with AI
     */
    private double evaluateCoherence(String essay) {
        double score = 5.0; // Base score

        // Check for transition words
        String[] transitions = { "firstly", "secondly", "finally", "in addition",
                "for example", "in conclusion", "on the other hand",
                "as a result", "in contrast", "similarly" };

        for (String trans : transitions) {
            if (essay.toLowerCase().contains(trans)) {
                score += 0.5;
            }
        }

        // Check paragraph structure
        String[] paragraphs = essay.split("\n\n+");
        if (paragraphs.length >= 3) {
            score += 1.5; // Good structure with intro, body, conclusion
        }

        // Check for logical flow indicators
        if (essay.toLowerCase().contains("because") ||
                essay.toLowerCase().contains("since") ||
                essay.toLowerCase().contains("due to")) {
            score += 0.5;
        }

        return Math.min(10.0, score);
    }

    /**
     * Evaluate task achievement
     * Mock implementation - TODO: Replace with AI
     */
    private double evaluateTaskAchievement(String essay, String questionContent) {
        double score = 5.0; // Base score

        int wordCount = essay.split("\\s+").length;

        // Check minimum word count (typically 250+ for IELTS essays)
        if (wordCount >= 250) {
            score += 2.0;
        } else if (wordCount >= 150) {
            score += 1.0;
        }

        // Check if response addresses the question topic (simplified keyword matching)
        String[] questionWords = questionContent.toLowerCase().split("\\s+");
        int matchCount = 0;
        for (String word : questionWords) {
            if (word.length() > 4 && essay.toLowerCase().contains(word)) {
                matchCount++;
            }
        }

        if (matchCount >= 3) {
            score += 2.0;
        } else if (matchCount >= 1) {
            score += 1.0;
        }

        return Math.min(10.0, score);
    }

    /**
     * Generate detailed feedback based on scores
     */
    private String generateFeedback(double grammar, double vocabulary,
            double coherence, double taskAchievement, String essay) {
        StringBuilder feedback = new StringBuilder();

        // Overall summary
        double avg = (grammar + vocabulary + coherence + taskAchievement) / 4.0;
        if (avg >= 8) {
            feedback.append("Excellent work! Your essay demonstrates strong writing skills. ");
        } else if (avg >= 6) {
            feedback.append("Good effort! Your essay shows solid writing ability with room for improvement. ");
        } else {
            feedback.append("Keep practicing! Here are areas to focus on for improvement. ");
        }

        feedback.append("\n\n");

        // Grammar feedback
        feedback.append("**Grammar & Mechanics** (").append(String.format("%.1f", grammar)).append("/10): ");
        if (grammar >= 8) {
            feedback.append("Excellent grammar with few errors.");
        } else if (grammar >= 6) {
            feedback.append("Good grammar overall. Minor errors present.");
        } else {
            feedback.append("Focus on sentence structure and punctuation.");
        }
        feedback.append("\n\n");

        // Vocabulary feedback
        feedback.append("**Vocabulary** (").append(String.format("%.1f", vocabulary)).append("/10): ");
        if (vocabulary >= 8) {
            feedback.append("Rich and varied vocabulary used effectively.");
        } else if (vocabulary >= 6) {
            feedback.append("Good word choice. Try using more advanced vocabulary.");
        } else {
            feedback.append("Work on expanding your vocabulary range.");
        }
        feedback.append("\n\n");

        // Coherence feedback
        feedback.append("**Coherence** (").append(String.format("%.1f", coherence)).append("/10): ");
        if (coherence >= 8) {
            feedback.append("Well-organized with clear logical flow.");
        } else if (coherence >= 6) {
            feedback.append("Good organization. Use more transition words for smoother flow.");
        } else {
            feedback.append("Focus on organizing ideas clearly with transitions.");
        }
        feedback.append("\n\n");

        // Task achievement feedback
        feedback.append("**Task Achievement** (").append(String.format("%.1f", taskAchievement)).append("/10): ");
        int wordCount = essay.split("\\s+").length;
        feedback.append("Word count: ").append(wordCount).append(". ");
        if (taskAchievement >= 8) {
            feedback.append("Excellent response to the prompt.");
        } else if (taskAchievement >= 6) {
            feedback.append("Good response. Ensure all parts of the question are addressed.");
        } else {
            feedback.append("Make sure to fully address the question prompt.");
        }

        return feedback.toString();
    }
}
