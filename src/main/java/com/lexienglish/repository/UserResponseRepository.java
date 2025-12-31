package com.lexienglish.repository;

import com.lexienglish.entity.UserResponse;
import com.lexienglish.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {

    List<UserResponse> findByTestAttemptOrderByQuestionOrderIndexAsc(TestAttempt testAttempt);

    Optional<UserResponse> findByTestAttemptAndQuestionId(TestAttempt testAttempt, Long questionId);

    long countByTestAttempt(TestAttempt testAttempt);

    long countByTestAttemptAndIsCorrectTrue(TestAttempt testAttempt);
}
