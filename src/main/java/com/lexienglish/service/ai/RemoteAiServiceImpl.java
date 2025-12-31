package com.lexienglish.service.ai;

import com.lexienglish.service.ai.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Remote AI Service Implementation using WebClient.
 * 
 * Implements circuit breaker pattern for fault tolerance.
 */
@Slf4j
@Service
public class RemoteAiServiceImpl implements RemoteAiService {

    private final WebClient webClient;

    public RemoteAiServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${ai-service.base-url:http://localhost:8000}") String baseUrl,
            @Value("${ai-service.timeout:30s}") Duration timeout) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("RemoteAiService initialized with baseUrl: {}", baseUrl);
    }

    // ==================== Document Parsing ====================

    @Override
    @CircuitBreaker(name = "aiService", fallbackMethod = "parseDocumentFallback")
    @Retry(name = "aiService")
    public DocumentParseResponse parseDocument(DocumentParseRequest request) {
        log.info("Parsing document: {}", request.getFilename());

        return webClient.post()
                .uri("/api/v1/document/parse")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DocumentParseResponse.class)
                .block();
    }

    @Override
    @CircuitBreaker(name = "aiService", fallbackMethod = "parseDocumentAsyncFallback")
    public AsyncParseResponse parseDocumentAsync(AsyncParseRequest request) {
        log.info("Starting async document parsing: {}", request.getFilename());

        return webClient.post()
                .uri("/api/v1/document/parse-async")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AsyncParseResponse.class)
                .block();
    }

    @Override
    @CircuitBreaker(name = "aiService")
    public TaskStatusResponse getTaskStatus(String taskId) {
        return webClient.get()
                .uri("/api/v1/document/task/{taskId}", taskId)
                .retrieve()
                .bodyToMono(TaskStatusResponse.class)
                .block();
    }

    // ==================== Scoring ====================

    @Override
    @CircuitBreaker(name = "aiService", fallbackMethod = "scoreWritingFallback")
    @Retry(name = "aiService")
    public WritingScoringResponse scoreWriting(WritingScoringRequest request) {
        log.info("Scoring writing ({} chars)", request.getText().length());

        return webClient.post()
                .uri("/api/v1/scoring/writing")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(WritingScoringResponse.class)
                .block();
    }

    @Override
    @CircuitBreaker(name = "aiService", fallbackMethod = "scoreSpeakingFallback")
    @Retry(name = "aiService")
    public SpeakingScoringResponse scoreSpeaking(SpeakingScoringRequest request) {
        log.info("Scoring speaking ({} chars)", request.getTranscript().length());

        return webClient.post()
                .uri("/api/v1/scoring/speaking")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SpeakingScoringResponse.class)
                .block();
    }

    // ==================== Chat ====================

    @Override
    @CircuitBreaker(name = "aiService", fallbackMethod = "chatCompleteFallback")
    public ChatCompletionResponse chatComplete(ChatCompletionRequest request) {
        log.info("Chat completion with {} messages", request.getMessages().size());

        return webClient.post()
                .uri("/api/v1/chat/complete")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();
    }

    // ==================== Health ====================

    @Override
    public boolean isHealthy() {
        try {
            String response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response != null && response.contains("healthy");
        } catch (Exception e) {
            log.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Fallback Methods ====================

    private DocumentParseResponse parseDocumentFallback(DocumentParseRequest request, Throwable t) {
        log.error("Document parsing fallback triggered: {}", t.getMessage());
        return DocumentParseResponse.builder()
                .success(false)
                .title(request.getFilename())
                .build();
    }

    private AsyncParseResponse parseDocumentAsyncFallback(AsyncParseRequest request, Throwable t) {
        log.error("Async parsing fallback triggered: {}", t.getMessage());
        return AsyncParseResponse.builder()
                .taskId("fallback")
                .status("FAILED")
                .message("AI service unavailable: " + t.getMessage())
                .build();
    }

    private WritingScoringResponse scoreWritingFallback(WritingScoringRequest request, Throwable t) {
        log.error("Writing scoring fallback triggered: {}", t.getMessage());
        return WritingScoringResponse.builder()
                .overallScore(0)
                .maxScore(request.getMaxScore())
                .percentage(0)
                .feedback("Scoring service temporarily unavailable. Please try again later.")
                .build();
    }

    private SpeakingScoringResponse scoreSpeakingFallback(SpeakingScoringRequest request, Throwable t) {
        log.error("Speaking scoring fallback triggered: {}", t.getMessage());
        return SpeakingScoringResponse.builder()
                .overallScore(0)
                .maxScore(request.getMaxScore())
                .percentage(0)
                .feedback("Scoring service temporarily unavailable. Please try again later.")
                .build();
    }

    private ChatCompletionResponse chatCompleteFallback(ChatCompletionRequest request, Throwable t) {
        log.error("Chat completion fallback triggered: {}", t.getMessage());
        return ChatCompletionResponse.builder()
                .message(ChatCompletionResponse.ChatMessage.builder()
                        .role("assistant")
                        .content("I'm sorry, I'm experiencing technical difficulties. Please try again in a moment.")
                        .build())
                .finishReason("error")
                .tokensUsed(0)
                .processingTimeMs(0)
                .build();
    }
}
