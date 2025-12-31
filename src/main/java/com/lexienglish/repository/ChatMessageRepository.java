package com.lexienglish.repository;

import com.lexienglish.entity.ChatMessage;
import com.lexienglish.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession session);

    Page<ChatMessage> findByChatSessionOrderByCreatedAtDesc(ChatSession session, Pageable pageable);

    List<ChatMessage> findTop20ByChatSessionOrderByCreatedAtDesc(ChatSession session);

    long countByChatSession(ChatSession session);
}
