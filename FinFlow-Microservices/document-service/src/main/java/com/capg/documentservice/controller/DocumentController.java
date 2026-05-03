package com.capg.documentservice.controller;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST controller for document management endpoints.
 * Base path: /documents
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

    /** Fetch all documents for a given loan application */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<java.util.List<Document>> getByApplicationId(@PathVariable Long applicationId) {
        log.info("GET /documents/application/{}", applicationId);
        return ResponseEntity.ok(service.getByApplicationId(applicationId));
    }

    /** Download a document file by document ID */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        log.info("GET /documents/{}/download", id);
        try {
            Document doc = service.getDocumentById(id);
            Path filePath = Paths.get(doc.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                log.warn("File not found on disk: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Extract original filename (after UUID prefix)
            String fileName = filePath.getFileName().toString();
            if (fileName.contains("_")) {
                fileName = fileName.substring(fileName.indexOf("_") + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Download failed for document {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

