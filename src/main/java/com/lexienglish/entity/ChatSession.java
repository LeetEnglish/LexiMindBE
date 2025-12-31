package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatSession entity for AI tutoring conversations.
 * Each session represents a conversation thread with the AI tutor.
 */
@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "context", columnDefinition = "TEXT")
    private String context; // Optional topic/context for the session

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    public enum SessionType {
        GENERAL_CHAT, // General English tutoring
        GRAMMAR_HELP, // Grammar questions & corrections
        VOCABULARY, // Vocabulary building
        WRITING_REVIEW, // Writing practice & feedback
        SPEAKING_PRACTICE, // Speaking conversation practice
        TEST_PREP // Test preparation discussions
    }

    public enum SessionStatus {
        ACTIVE,
        ARCHIVED,
        DELETED
    }

    // Helper method
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChatSession(this);
        messageCount++;
        lastMessageAt = LocalDateTime.now();
    }
}
