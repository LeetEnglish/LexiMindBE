package com.lexienglish.repository;

import com.lexienglish.entity.TestAttempt;
import com.lexienglish.entity.MockTest;
import com.lexienglish.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Page<TestAttempt> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<TestAttempt> findByUserAndStatusOrderByCreatedAtDesc(User user, TestAttempt.AttemptStatus status);

    Optional<TestAttempt> findByUserAndMockTestAndStatus(User user, MockTest mockTest,
            TestAttempt.AttemptStatus status);

    long countByUser(User user);

    long countByUserAndStatus(User user, TestAttempt.AttemptStatus status);

    @Query("SELECT AVG(t.percentageScore) FROM TestAttempt t WHERE t.user = :user AND t.status = 'COMPLETED'")
    Double getAverageScoreByUser(User user);

    @Query("SELECT t FROM TestAttempt t WHERE t.user = :user AND t.status = 'COMPLETED' ORDER BY t.percentageScore DESC")
    List<TestAttempt> findTopScoresByUser(User user, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TestAttempt t WHERE t.user = :user AND t.mockTest.testType = :testType AND t.status = 'COMPLETED'")
    long countCompletedByUserAndTestType(User user, MockTest.TestType testType);
}
