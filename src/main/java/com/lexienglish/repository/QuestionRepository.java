package com.lexienglish.repository;

import com.lexienglish.entity.Question;
import com.lexienglish.entity.MockTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByMockTestOrderByOrderIndexAsc(MockTest mockTest);

    long countByMockTest(MockTest mockTest);
}
