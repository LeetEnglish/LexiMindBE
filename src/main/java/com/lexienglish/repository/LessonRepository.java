package com.lexienglish.repository;

import com.lexienglish.entity.Document;
import com.lexienglish.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByDocumentOrderByOrderIndexAsc(Document document);

    Optional<Lesson> findByDocumentAndOrderIndex(Document document, Integer orderIndex);

    long countByDocument(Document document);

    long countByDocumentAndCompletedTrue(Document document);
}
