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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RemoteAiService remoteAiService;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        testSession = ChatSession.builder()
                .id(1L)
                .user(testUser)
                .title("Test Chat")
                .sessionType(ChatSession.SessionType.GENERAL_CHAT)
                .status(ChatSession.SessionStatus.ACTIVE)
                .build();
    }

    @Test
    void createSession_Success() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest("Test", "GENERAL_CHAT", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        ChatSessionDto result = chatService.createSession(request, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Chat");
        verify(chatSessionRepository).save(any(ChatSession.class));
    }

    @Test
    void createSession_InvalidSessionType_ThrowsException() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest("Test", "INVALID_TYPE", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> chatService.createSession(request, "test@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid session type");
    }

    @Test
    void getUserSessions_Success() {
        // Given
        Page<ChatSession> sessions = new PageImpl<>(List.of(testSession));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatSessionRepository.findByUserAndStatusOrderByLastMessageAtDesc(
                eq(testUser), eq(ChatSession.SessionStatus.ACTIVE), any()))
                .thenReturn(sessions);

        // When
        Page<ChatSessionDto> result = chatService.getUserSessions("test@example.com", null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void sendMessage_Success() {
        // Given
        SendMessageRequest request = new SendMessageRequest("Hello", false);

        ChatCompletionResponse aiResponse = ChatCompletionResponse.builder()
                .message(ChatCompletionResponse.ChatMessage.builder()
                        .role("assistant")
                        .content("Hello! How can I help you?")
                        .build())
                .tokensUsed(10)
                .processingTimeMs(100)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));
        when(chatMessageRepository.findTop20ByChatSessionOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(remoteAiService.chatComplete(any(ChatCompletionRequest.class))).thenReturn(aiResponse);

        // When
        ChatMessageDto result = chatService.sendMessage(1L, request, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello! How can I help you?");
        assertThat(result.getRole()).isEqualTo("ASSISTANT");
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void archiveSession_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(testSession);

        // When
        chatService.archiveSession(1L, "test@example.com");

        // Then
        verify(chatSessionRepository)
                .save(argThat(session -> session.getStatus() == ChatSession.SessionStatus.ARCHIVED));
    }

    @Test
    void getSession_NotFound_ThrowsException() {
        // Given
        when(chatSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> chatService.getSession(999L, "test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
