package com.lexienglish.repository;

import com.lexienglish.entity.Document;
import com.lexienglish.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Document> findByUserAndStatusOrderByCreatedAtDesc(User user, Document.ProcessingStatus status);

    @Query("SELECT d FROM Document d WHERE d.user = :user ORDER BY d.createdAt DESC")
    List<Document> findAllByUser(User user);

    long countByUser(User user);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.user = :user AND d.status = 'COMPLETED'")
    long countCompletedByUser(User user);
}
