package com.lexienglish.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ChatMessage entity for individual messages in a chat session.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    // For writing analysis messages
    @Column(name = "grammar_issues", columnDefinition = "TEXT")
    private String grammarIssues; // JSON array of grammar issues

    @Column(name = "vocabulary_suggestions", columnDefinition = "TEXT")
    private String vocabularySuggestions; // JSON array of vocabulary suggestions

    @Column(name = "corrected_text", columnDefinition = "TEXT")
    private String correctedText; // AI-corrected version of user text

    @Column(name = "improvement_score")
    private java.math.BigDecimal improvementScore; // 0-10 score for improvement areas

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageRole {
        USER,
        ASSISTANT,
        SYSTEM
    }
}
