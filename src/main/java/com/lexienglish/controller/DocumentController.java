package com.lexienglish.controller;

import com.lexienglish.dto.document.DocumentDto;
import com.lexienglish.dto.document.LessonDto;
import com.lexienglish.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document upload and management")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for AI processing")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal UserDetails userDetails) {

        DocumentDto document = documentService.uploadDocument(file, title, description, userDetails.getUsername());
        return ResponseEntity.ok(document);
    }

    @GetMapping
    @Operation(summary = "Get all user documents")
    public ResponseEntity<Page<DocumentDto>> getUserDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        return ResponseEntity.ok(documentService.getUserDocuments(userDetails.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(documentService.getDocument(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/lessons")
    @Operation(summary = "Get all lessons for a document")
    public ResponseEntity<List<LessonDto>> getDocumentLessons(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(documentService.getDocumentLessons(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        documentService.deleteDocument(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
    }
}
