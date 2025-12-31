package com.lexienglish.controller;

import com.lexienglish.dto.document.LessonDto;
import com.lexienglish.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Lesson management")
public class LessonController {

    private final DocumentService documentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID")
    public ResponseEntity<LessonDto> getLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(documentService.getLesson(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark lesson as completed")
    public ResponseEntity<LessonDto> markLessonCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(documentService.markLessonCompleted(id, userDetails.getUsername()));
    }
}
