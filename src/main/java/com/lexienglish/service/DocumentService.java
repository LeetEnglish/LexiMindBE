package com.lexienglish.service;

import com.lexienglish.dto.document.DocumentDto;
import com.lexienglish.dto.document.LessonDto;
import com.lexienglish.entity.Document;
import com.lexienglish.entity.Lesson;
import com.lexienglish.entity.User;
import com.lexienglish.exception.BadRequestException;
import com.lexienglish.exception.ResourceNotFoundException;
import com.lexienglish.repository.DocumentRepository;
import com.lexienglish.repository.LessonRepository;
import com.lexienglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DocumentParsingService documentParsingService;

    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, String title, String description, String userEmail) {
        User user = getUserByEmail(userEmail);

        // Store file
        String filePath = fileStorageService.storeFile(file, user.getId());

        // Create document entity
        Document document = Document.builder()
                .title(title != null ? title : file.getOriginalFilename())
                .description(description)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileType(fileStorageService.getFileType(file.getOriginalFilename()))
                .fileSize(file.getSize())
                .status(Document.ProcessingStatus.PENDING)
                .user(user)
                .build();

        document = documentRepository.save(document);

        // Trigger async processing
        processDocumentAsync(document.getId());

        log.info("Document uploaded: {} by user: {}", document.getId(), userEmail);
        return DocumentDto.fromEntity(document);
    }

    @Async
    public void processDocumentAsync(Long documentId) {
        try {
            documentParsingService.parseDocument(documentId);
        } catch (Exception e) {
            log.error("Failed to process document: {}", documentId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<DocumentDto> getUserDocuments(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return documentRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(DocumentDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(Long id, String userEmail) {
        Document document = getDocumentForUser(id, userEmail);
        return DocumentDto.fromEntity(document);
    }

    @Transactional(readOnly = true)
    public List<LessonDto> getDocumentLessons(Long documentId, String userEmail) {
        Document document = getDocumentForUser(documentId, userEmail);
        List<Lesson> lessons = lessonRepository.findByDocumentOrderByOrderIndexAsc(document);
        return lessons.stream().map(LessonDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public LessonDto getLesson(Long lessonId, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        // Verify user owns the document
        if (!lesson.getDocument().getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }

        return LessonDto.fromEntity(lesson);
    }

    @Transactional
    public LessonDto markLessonCompleted(Long lessonId, String userEmail) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        Document document = lesson.getDocument();
        if (!document.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Lesson", "id", lessonId);
        }

        if (!lesson.isCompleted()) {
            lesson.setCompleted(true);
            lessonRepository.save(lesson);

            // Update document progress
            document.setCompletedLessons(document.getCompletedLessons() + 1);
            documentRepository.save(document);
        }

        return LessonDto.fromEntity(lesson);
    }

    @Transactional
    public void deleteDocument(Long id, String userEmail) {
        Document document = getDocumentForUser(id, userEmail);

        // Delete file
        fileStorageService.deleteFile(document.getFilePath());

        // Delete document (cascades to lessons, flashcards)
        documentRepository.delete(document);

        log.info("Document deleted: {} by user: {}", id, userEmail);
    }

    private Document getDocumentForUser(Long id, String userEmail) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        if (!document.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Document", "id", id);
        }

        return document;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
