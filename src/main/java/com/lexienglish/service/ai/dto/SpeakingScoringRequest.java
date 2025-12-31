package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for AI speaking scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingScoringRequest {
    private String transcript;
    private String audioUrl;
    private String prompt;
    @Builder.Default
    private double maxScore = 10.0;
}
