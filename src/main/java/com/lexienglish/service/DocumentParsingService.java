package com.lexienglish.service;

/**
 * Interface for document parsing service.
 * Implementations can use different AI providers or mock data.
 */
public interface DocumentParsingService {

    /**
     * Parse a document and generate lessons, flashcards, and exercises.
     * This method should be called asynchronously after document upload.
     * 
     * @param documentId The ID of the document to parse
     */
    void parseDocument(Long documentId);
}
