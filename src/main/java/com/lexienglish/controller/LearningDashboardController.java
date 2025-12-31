package com.lexienglish.controller;

import com.lexienglish.service.LearningDashboardService;
import com.lexienglish.service.RevisionSchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Learning Dashboard Controller
 * 
 * Provides centralized API for user's learning progress and recommendations.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class LearningDashboardController {

    private final LearningDashboardService dashboardService;
    private final RevisionSchedulingService revisionService;

    @GetMapping
    public ResponseEntity<LearningDashboardService.DashboardData> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        var data = dashboardService.getDashboard(userDetails.getUsername());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<LearningDashboardService.RecommendedAction>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        var recommendations = dashboardService.getRecommendations(userDetails.getUsername());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/revision/stats")
    public ResponseEntity<RevisionSchedulingService.RevisionStats> getRevisionStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        var stats = revisionService.getRevisionStats(userDetails.getUsername());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revision/schedule")
    public ResponseEntity<List<RevisionSchedulingService.DailySchedule>> getWeeklySchedule(
            @AuthenticationPrincipal UserDetails userDetails) {
        var schedule = revisionService.getWeeklySchedule(userDetails.getUsername());
        return ResponseEntity.ok(schedule);
    }
}
