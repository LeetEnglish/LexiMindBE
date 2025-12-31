package com.lexienglish.service;

import com.lexienglish.entity.Document;
import com.lexienglish.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * Store uploaded file and return the storage path
     */
    public String storeFile(MultipartFile file, Long userId) {
        validateFile(file);

        try {
            // Create user directory if not exists
            Path userDir = Paths.get(uploadDir, "user-" + userId);
            Files.createDirectories(userDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + "." + extension;

            // Store file
            Path targetPath = userDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {} for user: {}", storedFilename, userId);
            return targetPath.toString();

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    /**
     * Delete stored file
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }

    /**
     * Read file content as text
     */
    public String readFileContent(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            throw new BadRequestException("Failed to read file content");
        }
    }

    /**
     * Read file as byte array for binary content
     */
    public byte[] readFileBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to read file bytes: {}", filePath, e);
            throw new BadRequestException("Failed to read file content");
        }
    }

    public Document.FileType getFileType(String filename) {
        String ext = getFileExtension(filename).toUpperCase();
        return switch (ext) {
            case "PDF" -> Document.FileType.PDF;
            case "DOCX" -> Document.FileType.DOCX;
            case "TXT" -> Document.FileType.TXT;
            default -> throw new BadRequestException("Unsupported file type: " + ext);
        };
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed (50MB)");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Supported: PDF, DOCX, TXT");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BadRequestException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
