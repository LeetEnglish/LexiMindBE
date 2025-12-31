package com.lexienglish.dto.document;

import com.lexienglish.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DocumentDto fromEntity(Document doc) {
        return DocumentDto.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .fileName(doc.getFileName())
                .fileType(doc.getFileType().name())
                .fileSize(doc.getFileSize())
                .status(doc.getStatus().name())
                .totalLessons(doc.getTotalLessons())
                .completedLessons(doc.getCompletedLessons())
                .progressPercentage(doc.getProgressPercentage())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
