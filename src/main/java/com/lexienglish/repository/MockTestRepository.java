package com.lexienglish.repository;

import com.lexienglish.entity.MockTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    Page<MockTest> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    List<MockTest> findByPublishedTrueAndTestTypeOrderByCreatedAtDesc(MockTest.TestType testType);

    List<MockTest> findByPublishedTrueAndSkillTypeOrderByCreatedAtDesc(MockTest.SkillType skillType);

    @Query("SELECT m FROM MockTest m WHERE m.published = true " +
            "AND (:testType IS NULL OR m.testType = :testType) " +
            "AND (:skillType IS NULL OR m.skillType = :skillType)")
    Page<MockTest> findByFilters(MockTest.TestType testType, MockTest.SkillType skillType, Pageable pageable);

    long countByPublishedTrue();
}
