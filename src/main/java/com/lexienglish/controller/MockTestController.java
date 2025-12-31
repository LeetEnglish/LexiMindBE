package com.lexienglish.controller;

import com.lexienglish.dto.mocktest.*;
import com.lexienglish.service.MockTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mock-tests")
@RequiredArgsConstructor
@Tag(name = "Mock Tests", description = "Mock test catalog and test-taking")
public class MockTestController {

    private final MockTestService mockTestService;

    // ==================== Test Catalog ====================

    @GetMapping
    @Operation(summary = "Get available mock tests")
    public ResponseEntity<Page<MockTestDto>> getAvailableTests(Pageable pageable) {
        return ResponseEntity.ok(mockTestService.getAvailableTests(pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter tests by type and skill")
    public ResponseEntity<Page<MockTestDto>> getTestsByFilter(
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String skillType,
            Pageable pageable) {
        return ResponseEntity.ok(mockTestService.getTestsByFilter(testType, skillType, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test details")
    public ResponseEntity<MockTestDto> getTestDetails(@PathVariable Long id) {
        return ResponseEntity.ok(mockTestService.getTestDetails(id));
    }

    // ==================== Taking Tests ====================

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a new test attempt")
    public ResponseEntity<TestAttemptDto> startTest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mockTestService.startTest(id, userDetails.getUsername()));
    }

    @GetMapping("/attempts/{attemptId}/questions")
    @Operation(summary = "Get questions for a test attempt")
    public ResponseEntity<List<QuestionDto>> getTestQuestions(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mockTestService.getTestQuestions(attemptId, userDetails.getUsername()));
    }

    @PostMapping("/attempts/{attemptId}/answer")
    @Operation(summary = "Submit an answer for a question")
    public ResponseEntity<Map<String, String>> submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        mockTestService.submitAnswer(attemptId, request, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Answer submitted successfully"));
    }

    @PostMapping("/attempts/{attemptId}/complete")
    @Operation(summary = "Complete a test attempt")
    public ResponseEntity<TestAttemptDto> completeTest(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mockTestService.completeTest(attemptId, userDetails.getUsername()));
    }

    // ==================== User History ====================

    @GetMapping("/attempts")
    @Operation(summary = "Get user's test attempts")
    public ResponseEntity<Page<TestAttemptDto>> getUserAttempts(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(mockTestService.getUserAttempts(userDetails.getUsername(), pageable));
    }

    @GetMapping("/attempts/{attemptId}")
    @Operation(summary = "Get test attempt result")
    public ResponseEntity<TestAttemptDto> getAttemptResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mockTestService.getAttemptResult(attemptId, userDetails.getUsername()));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get user's test analytics")
    public ResponseEntity<TestAnalyticsDto> getUserAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(mockTestService.getUserAnalytics(userDetails.getUsername()));
    }
}
