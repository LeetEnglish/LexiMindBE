package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for AI writing scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingScoringRequest {
    private String text;
    private String prompt;
    @Builder.Default
    private double maxScore = 10.0;
}
