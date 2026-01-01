package com.lexienglish.service;

import com.lexienglish.entity.UserResponse;
import com.lexienglish.service.ai.RemoteAiService;
import com.lexienglish.service.ai.dto.WritingScoringRequest;
import com.lexienglish.service.ai.dto.WritingScoringResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Remote Writing Scoring Service
 * 
 * Calls Python AI microservice for writing evaluation.
 * Activated when ai-service.enabled=true in application.yml
 * 
 * Replaces the local WritingScoringService.
 */
@Slf4j
@Service("remoteWritingScoringService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-service.enabled", havingValue = "true")
public class RemoteWritingScoringService {

    private final RemoteAiService remoteAiService;

    /**
     * Score a writing response using remote AI service.
     * 
     * @param response The user's response containing the essay
     * @return The updated response with scores and feedback
     */
    public UserResponse scoreWritingResponse(UserResponse response) {
        String essay = response.getUserAnswer();

        if (essay == null || essay.trim().isEmpty()) {
            response.setScore(java.math.BigDecimal.ZERO);
            response.setAiFeedback("No response provided.");
            return response;
        }

        log.info("Remote AI scoring writing response for question: {}", response.getQuestion().getId());

        // Build request
        WritingScoringRequest request = WritingScoringRequest.builder()
                .text(essay)
                .prompt(response.getQuestion().getContent())
                .maxScore(response.getQuestion().getPoints().doubleValue())
                .build();

        // Call AI service
        WritingScoringResponse aiResponse = remoteAiService.scoreWriting(request);

        // Map response to UserResponse entity
        response.setScore(java.math.BigDecimal.valueOf(aiResponse.getOverallScore()));
        response.setAiFeedback(aiResponse.getFeedback());

        if (aiResponse.getDetails() != null) {
            response.setGrammarScore(java.math.BigDecimal.valueOf(aiResponse.getDetails().getGrammarScore()));
            response.setVocabularyScore(java.math.BigDecimal.valueOf(aiResponse.getDetails().getVocabularyScore()));
            response.setCoherenceScore(java.math.BigDecimal.valueOf(aiResponse.getDetails().getCoherenceScore()));
            response.setTaskAchievementScore(java.math.BigDecimal.valueOf(aiResponse.getDetails().getTaskAchievementScore()));
        }

        // Passing threshold (60%)
        response.setIsCorrect(aiResponse.getPercentage() >= 60.0);

        log.info("Remote AI writing score: {} / {} ({}%)",
                aiResponse.getOverallScore(), aiResponse.getMaxScore(), aiResponse.getPercentage());

        return response;
    }
}
