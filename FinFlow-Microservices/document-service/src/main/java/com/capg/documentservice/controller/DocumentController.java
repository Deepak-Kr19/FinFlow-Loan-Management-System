package com.capg.documentservice.controller;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for document management endpoints.
 * Base path: /documents
 *
 * Endpoints:
 * - POST /documents/upload      → Upload a document for a loan application
 * - PUT  /documents/{id}/verify → Verify or reject a document (admin)
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    /** Upload a document file for a specific loan application */
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /documents/upload — applicationId: {}, type: {}, file: {}", applicationId, type, file.getOriginalFilename());
        try {
            return ResponseEntity.ok(service.uploadDocument(applicationId, type, file));
        } catch (Exception e) {
            log.error("Document upload failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /** Admin endpoint: verify or reject a document by ID */
    @PutMapping("/{id}/verify")
    public ResponseEntity<Document> verifyDocument(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        log.info("PUT /documents/{}/verify — status: {}", id, status);
        return ResponseEntity.ok(service.verifyDocument(id, status));
    }
}
