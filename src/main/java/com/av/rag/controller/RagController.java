package com.av.rag.controller;

import com.av.rag.service.DocumentIngestionService;
import com.av.rag.service.RetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for RAG (Retrieval Augmented Generation) operations.
 * Provides endpoints for document upload and question answering.
 */
@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RagController {

    private static final Logger logger = LoggerFactory.getLogger(RagController.class);

    @Autowired
    private DocumentIngestionService documentIngestionService;
    @Autowired
    private RetrievalService retrievalService;

    /**
     * Upload documents for ingestion and indexing.
     * @param files List of files to upload
     * @return Response indicating success or failure
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                logger.warn("Upload request received with no files");
                return ResponseEntity.badRequest().body(Map.of("error", "No files provided"));
            }

            if (files.stream().anyMatch(MultipartFile::isEmpty)) {
                logger.warn("Upload request received with empty files");
                return ResponseEntity.badRequest().body(Map.of("error", "One or more files are empty"));
            }

            logger.info("Uploading {} documents", files.size());
            documentIngestionService.processAndStore(files);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Documents uploaded and indexed successfully");
            response.put("fileCount", files.size());
            logger.info("Successfully processed {} documents", files.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error during document upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process documents: " + e.getMessage()));
        }
    }

    /**
     * Ask a question about indexed documents.
     * @param question The question to ask
     * @return AI-generated answer based on relevant documents
     */
    @GetMapping("/ask")
    public ResponseEntity<?> ask(@RequestParam("question") String question) {
        try {
            if (question == null || question.trim().isEmpty()) {
                logger.warn("Ask request received with empty question");
                return ResponseEntity.badRequest().body(Map.of("error", "Question cannot be empty"));
            }

            logger.info("Processing question: {}", question);
            String answer = retrievalService.ask(question);

            Map<String, Object> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            logger.info("Successfully generated answer");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error during question answering", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process question: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * @return Application status
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
