package com.lexienglish.dto.mocktest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Answer is required")
    private String answer;

    private Integer timeSpentSeconds;
}
