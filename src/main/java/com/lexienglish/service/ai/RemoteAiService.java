package com.lexienglish.service.ai;

import com.lexienglish.service.ai.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Remote AI Service Interface
 * 
 * Follows Open/Closed Principle - implementations can be swapped
 * by changing configuration without modifying core logic.
 */
public interface RemoteAiService {

    // ==================== Document Parsing ====================

    /**
     * Parse a document synchronously.
     * 
     * @param request Document parse request
     * @return Parsed document with lessons and flashcards
     */
    DocumentParseResponse parseDocument(DocumentParseRequest request);

    /**
     * Parse a document asynchronously.
     * 
     * @param request Async parse request
     * @return Task ID for polling
     */
    AsyncParseResponse parseDocumentAsync(AsyncParseRequest request);

    /**
     * Get status of async parsing task.
     * 
     * @param taskId Celery task ID
     * @return Current status and result if complete
     */
    TaskStatusResponse getTaskStatus(String taskId);

    // ==================== Scoring ====================

    /**
     * Score a writing response using AI.
     * 
     * @param request Writing scoring request
     * @return Scores and feedback
     */
    WritingScoringResponse scoreWriting(WritingScoringRequest request);

    /**
     * Score a speaking response using AI.
     * 
     * @param request Speaking scoring request
     * @return Scores and feedback
     */
    SpeakingScoringResponse scoreSpeaking(SpeakingScoringRequest request);

    // ==================== Chat ====================

    /**
     * Generate a chat completion for tutoring.
     * 
     * @param request Chat completion request
     * @return AI response
     */
    ChatCompletionResponse chatComplete(ChatCompletionRequest request);

    // ==================== Health ====================

    /**
     * Check if AI service is healthy.
     * 
     * @return true if service is available
     */
    boolean isHealthy();
}
