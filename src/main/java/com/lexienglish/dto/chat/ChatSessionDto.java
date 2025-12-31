package com.lexienglish.dto.chat;

import com.lexienglish.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDto {
    private Long id;
    private String title;
    private String sessionType;
    private String status;
    private String context;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public static ChatSessionDto fromEntity(ChatSession entity) {
        return ChatSessionDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sessionType(entity.getSessionType().name())
                .status(entity.getStatus().name())
                .context(entity.getContext())
                .messageCount(entity.getMessageCount())
                .createdAt(entity.getCreatedAt())
                .lastMessageAt(entity.getLastMessageAt())
                .build();
    }
}
