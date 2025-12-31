package com.lexienglish.repository;

import com.lexienglish.entity.ChatSession;
import com.lexienglish.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Page<ChatSession> findByUserAndStatusOrderByLastMessageAtDesc(
            User user, ChatSession.SessionStatus status, Pageable pageable);

    Page<ChatSession> findByUserAndStatusAndSessionTypeOrderByLastMessageAtDesc(
            User user, ChatSession.SessionStatus status,
            ChatSession.SessionType sessionType, Pageable pageable);

    List<ChatSession> findTop10ByUserAndStatusOrderByLastMessageAtDesc(
            User user, ChatSession.SessionStatus status);

    long countByUserAndStatus(User user, ChatSession.SessionStatus status);

    @Query("SELECT COALESCE(SUM(cs.messageCount), 0) FROM ChatSession cs WHERE cs.user = :user")
    long countTotalMessagesByUser(User user);
}
