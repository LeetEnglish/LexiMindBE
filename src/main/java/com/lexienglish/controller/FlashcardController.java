package com.lexienglish.controller;

import com.lexienglish.dto.document.CreateFlashcardRequest;
import com.lexienglish.dto.document.FlashcardDto;
import com.lexienglish.dto.document.FlashcardReviewRequest;
import com.lexienglish.service.FlashcardService;
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
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcards", description = "Flashcard management and SRS review")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    @Operation(summary = "Create a new flashcard")
    public ResponseEntity<FlashcardDto> createFlashcard(
            @Valid @RequestBody CreateFlashcardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(flashcardService.createFlashcard(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Get all user flashcards")
    public ResponseEntity<Page<FlashcardDto>> getUserFlashcards(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        return ResponseEntity.ok(flashcardService.getUserFlashcards(userDetails.getUsername(), pageable));
    }

    @GetMapping("/due")
    @Operation(summary = "Get flashcards due for review")
    public ResponseEntity<List<FlashcardDto>> getDueFlashcards(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(flashcardService.getDueFlashcards(userDetails.getUsername()));
    }

    @GetMapping("/due/paged")
    @Operation(summary = "Get flashcards due for review (paginated)")
    public ResponseEntity<Page<FlashcardDto>> getDueFlashcardsPaged(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        return ResponseEntity.ok(flashcardService.getDueFlashcards(userDetails.getUsername(), pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get flashcard statistics")
    public ResponseEntity<FlashcardService.FlashcardStats> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(flashcardService.getStats(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flashcard by ID")
    public ResponseEntity<FlashcardDto> getFlashcard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(flashcardService.getFlashcard(id, userDetails.getUsername()));
    }

    @PostMapping("/review")
    @Operation(summary = "Review a flashcard with quality rating (SM-2 algorithm)")
    public ResponseEntity<FlashcardDto> reviewFlashcard(
            @Valid @RequestBody FlashcardReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(flashcardService.reviewFlashcard(
                request.getFlashcardId(),
                request.getQuality(),
                userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a flashcard")
    public ResponseEntity<Map<String, String>> deleteFlashcard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        flashcardService.deleteFlashcard(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Flashcard deleted successfully"));
    }
}
