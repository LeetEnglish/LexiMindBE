package com.lexienglish.controller;

import com.lexienglish.dto.chat.*;
import com.lexienglish.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ChatController exposes REST endpoints for AI tutoring chat.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ==================== Session Endpoints ====================

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionDto> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatSessionDto session = chatService.createSession(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<ChatSessionDto>> getUserSessions(
            @RequestParam(required = false) String sessionType,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<ChatSessionDto> sessions = chatService.getUserSessions(
                userDetails.getUsername(), sessionType, pageable);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionDto> getSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatSessionDto session = chatService.getSession(sessionId, userDetails.getUsername());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getSessionMessages(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ChatMessageDto> messages = chatService.getSessionMessages(sessionId, userDetails.getUsername());
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> archiveSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.archiveSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ==================== Message Endpoints ====================

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatMessageDto response = chatService.sendMessage(sessionId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
