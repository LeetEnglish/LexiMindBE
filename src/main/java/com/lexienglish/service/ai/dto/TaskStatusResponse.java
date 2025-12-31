package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Task status response for async operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusResponse {
    private String taskId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int progress; // 0-100
    private DocumentParseResponse result;
    private String error;
}
