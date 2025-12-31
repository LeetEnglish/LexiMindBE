package com.lexienglish.service;

import com.lexienglish.dto.chat.*;
import com.lexienglish.entity.ChatMessage;
import com.lexienglish.entity.ChatSession;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.exception.ResourceNotFoundException;
import com.lexienglish.repository.ChatMessageRepository;
import com.lexienglish.repository.ChatSessionRepository;
import com.lexienglish.repository.UserRepository;
import com.lexienglish.service.ai.RemoteAiService;
import com.lexienglish.service.ai.dto.ChatCompletionRequest;
import com.lexienglish.service.ai.dto.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatService handles AI tutoring conversations.
 * Uses RemoteAiService to communicate with Python AI microservice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RemoteAiService remoteAiService;

    // ==================== Session Management ====================

    @Transactional
    public ChatSessionDto createSession(CreateSessionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        ChatSession.SessionType sessionType;
        try {
            sessionType = ChatSession.SessionType.valueOf(request.getSessionType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid session type: " + request.getSessionType());
        }

        ChatSession session = ChatSession.builder()
                .user(user)
                .title(request.getTitle())
                .sessionType(sessionType)
                .context(request.getContext())
                .status(ChatSession.SessionStatus.ACTIVE)
                .build();

        session = chatSessionRepository.save(session);
        log.info("Created chat session {} for user {}", session.getId(), userEmail);

        return ChatSessionDto.fromEntity(session);
    }

    @Transactional(readOnly = true)
    public Page<ChatSessionDto> getUserSessions(String userEmail, String sessionType, Pageable pageable) {
        User user = getUserByEmail(userEmail);

        if (sessionType != null && !sessionType.isEmpty()) {
            ChatSession.SessionType type = ChatSession.SessionType.valueOf(sessionType);
            return chatSessionRepository.findByUserAndStatusAndSessionTypeOrderByLastMessageAtDesc(
                    user, ChatSession.SessionStatus.ACTIVE, type, pageable)
                    .map(ChatSessionDto::fromEntity);
        }

        return chatSessionRepository.findByUserAndStatusOrderByLastMessageAtDesc(
                user, ChatSession.SessionStatus.ACTIVE, pageable)
                .map(ChatSessionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ChatSessionDto getSession(Long sessionId, String userEmail) {
        ChatSession session = getSessionForUser(sessionId, userEmail);
        return ChatSessionDto.fromEntity(session);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getSessionMessages(Long sessionId, String userEmail) {
        ChatSession session = getSessionForUser(sessionId, userEmail);
        return chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session)
                .stream()
                .map(ChatMessageDto::fromEntity)
                .toList();
    }

    @Transactional
    public void archiveSession(Long sessionId, String userEmail) {
        ChatSession session = getSessionForUser(sessionId, userEmail);
        session.setStatus(ChatSession.SessionStatus.ARCHIVED);
        chatSessionRepository.save(session);
        log.info("Archived chat session {} for user {}", sessionId, userEmail);
    }

    // ==================== Messaging ====================

    @Transactional
    public ChatMessageDto sendMessage(Long sessionId, SendMessageRequest request, String userEmail) {
        ChatSession session = getSessionForUser(sessionId, userEmail);

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getContent())
                .build();
        chatMessageRepository.save(userMessage);
        session.addMessage(userMessage);

        // Get AI response
        ChatMessage aiMessage = generateAiResponse(session, request.getContent(), request.isAnalyzeWriting());
        chatMessageRepository.save(aiMessage);
        session.addMessage(aiMessage);

        chatSessionRepository.save(session);

        log.info("Processed message in session {} for user {}", sessionId, userEmail);
        return ChatMessageDto.fromEntity(aiMessage);
    }

    private ChatMessage generateAiResponse(ChatSession session, String userMessage, boolean analyzeWriting) {
        // Build conversation history for context
        List<ChatCompletionRequest.ChatMessage> messages = new ArrayList<>();

        // Add system prompt based on session type
        String systemPrompt = getSystemPromptForSessionType(session.getSessionType());

        // Add recent message history (last 10 messages for context)
        List<ChatMessage> history = chatMessageRepository.findTop20ByChatSessionOrderByCreatedAtDesc(session);
        for (int i = Math.min(history.size() - 1, 9); i >= 0; i--) {
            ChatMessage msg = history.get(i);
            messages.add(ChatCompletionRequest.ChatMessage.builder()
                    .role(msg.getRole().name().toLowerCase())
                    .content(msg.getContent())
                    .build());
        }

        // Add current user message
        messages.add(ChatCompletionRequest.ChatMessage.builder()
                .role("user")
                .content(userMessage)
                .build());

        // Call AI service
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(messages)
                .systemPrompt(systemPrompt)
                .temperature(0.7)
                .maxTokens(512)
                .build();

        ChatCompletionResponse response = remoteAiService.chatComplete(request);

        // Build AI message
        ChatMessage.ChatMessageBuilder aiMessageBuilder = ChatMessage.builder()
                .chatSession(session)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content(response.getMessage().getContent())
                .tokensUsed(response.getTokensUsed())
                .processingTimeMs(response.getProcessingTimeMs());

        // Add writing analysis if requested
        if (analyzeWriting) {
            addWritingAnalysis(aiMessageBuilder, userMessage);
        }

        return aiMessageBuilder.build();
    }

    private String getSystemPromptForSessionType(ChatSession.SessionType type) {
        return switch (type) {
            case GENERAL_CHAT ->
                "You are a friendly English language tutor. Help the user practice English conversation, correct any mistakes gently, and explain language concepts clearly.";
            case GRAMMAR_HELP ->
                "You are an English grammar expert. Help the user understand grammar rules, correct their grammar mistakes, and provide clear explanations with examples.";
            case VOCABULARY ->
                "You are a vocabulary coach. Help the user learn new words, explain meanings, usage, and provide example sentences. Suggest synonyms and related vocabulary.";
            case WRITING_REVIEW ->
                "You are a writing tutor. Review the user's writing, provide constructive feedback on grammar, vocabulary, structure, and clarity. Suggest improvements while being encouraging.";
            case SPEAKING_PRACTICE ->
                "You are a speaking practice partner. Engage in natural conversation, help with pronunciation tips, and correct mistakes in a supportive way.";
            case TEST_PREP ->
                "You are a test preparation coach for English exams like IELTS, TOEFL. Help with practice questions, explain strategies, and provide feedback on responses.";
        };
    }

    private void addWritingAnalysis(ChatMessage.ChatMessageBuilder builder, String text) {
        // Basic analysis (in production, could call a separate analysis endpoint)
        List<String> grammarIssues = new ArrayList<>();
        List<String> vocabSuggestions = new ArrayList<>();

        // Simple checks
        if (!text.matches("^[A-Z].*")) {
            grammarIssues.add("Start sentences with a capital letter");
        }
        if (!text.matches(".*[.!?]$")) {
            grammarIssues.add("End sentences with proper punctuation");
        }
        if (text.contains("  ")) {
            grammarIssues.add("Avoid double spaces");
        }

        builder.grammarIssues(grammarIssues.isEmpty() ? null : String.join("; ", grammarIssues));
        builder.vocabularySuggestions(vocabSuggestions.isEmpty() ? null : String.join("; ", vocabSuggestions));
    }

    // ==================== Helper Methods ====================

    private ChatSession getSessionForUser(Long sessionId, String userEmail) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));

        if (!session.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("ChatSession", "id", sessionId);
        }

        return session;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
