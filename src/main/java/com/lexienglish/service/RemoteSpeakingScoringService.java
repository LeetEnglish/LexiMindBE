package com.lexienglish.service;

import com.lexienglish.entity.UserResponse;
import com.lexienglish.service.ai.RemoteAiService;
import com.lexienglish.service.ai.dto.SpeakingScoringRequest;
import com.lexienglish.service.ai.dto.SpeakingScoringResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Remote Speaking Scoring Service
 * 
 * Calls Python AI microservice for speaking evaluation.
 * Activated when ai-service.enabled=true in application.yml
 * 
 * Replaces the local SpeakingScoringService.
 */
@Slf4j
@Service("remoteSpeakingScoringService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-service.enabled", havingValue = "true")
public class RemoteSpeakingScoringService {

    private final RemoteAiService remoteAiService;

    /**
     * Score a speaking response using remote AI service.
     * 
     * @param response The user's response containing the transcript
     * @return The updated response with scores and feedback
     */
    public UserResponse scoreSpeakingResponse(UserResponse response) {
        String transcript = response.getUserAnswer();

        if (transcript == null || transcript.trim().isEmpty()) {
            response.setScore(0.0);
            response.setAiFeedback("No response provided.");
            return response;
        }

        log.info("Remote AI scoring speaking response for question: {}", response.getQuestion().getId());

        // Build request
        SpeakingScoringRequest request = SpeakingScoringRequest.builder()
                .transcript(transcript)
                .audioUrl(response.getAudioResponseUrl())
                .prompt(response.getQuestion().getContent())
                .maxScore(response.getQuestion().getPoints().doubleValue())
                .build();

        // Call AI service
        SpeakingScoringResponse aiResponse = remoteAiService.scoreSpeaking(request);

        // Map response to UserResponse entity
        response.setScore(aiResponse.getOverallScore());
        response.setAiFeedback(aiResponse.getFeedback());

        if (aiResponse.getDetails() != null) {
            response.setPronunciationScore(aiResponse.getDetails().getPronunciationScore());
            response.setFluencyScore(aiResponse.getDetails().getFluencyScore());
            response.setVocabularyScore(aiResponse.getDetails().getVocabularyScore());
            response.setGrammarScore(aiResponse.getDetails().getGrammarScore());
        }

        // Passing threshold (60%)
        response.setIsCorrect(aiResponse.getPercentage() >= 60.0);

        log.info("Remote AI speaking score: {} / {} ({}%)",
                aiResponse.getOverallScore(), aiResponse.getMaxScore(), aiResponse.getPercentage());

        return response;
    }
}
