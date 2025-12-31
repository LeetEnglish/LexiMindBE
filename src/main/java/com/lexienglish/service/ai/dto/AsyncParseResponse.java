package com.lexienglish.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from async parse initiation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncParseResponse {
    private String taskId;
    private String status;
    private String message;
}
