package com.lexienglish.dto.mocktest;

import com.lexienglish.entity.MockTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockTestDto {
    private Long id;
    private String title;
    private String description;
    private String testType;
    private String skillType;
    private Integer durationMinutes;
    private Integer totalQuestions;
    private Integer passingScore;
    private String difficultyLevel;
    private LocalDateTime createdAt;

    public static MockTestDto fromEntity(MockTest test) {
        return MockTestDto.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .testType(test.getTestType().name())
                .skillType(test.getSkillType().name())
                .durationMinutes(test.getDurationMinutes())
                .totalQuestions(test.getTotalQuestions())
                .passingScore(test.getPassingScore())
                .difficultyLevel(test.getDifficultyLevel().name())
                .createdAt(test.getCreatedAt())
                .build();
    }
}
