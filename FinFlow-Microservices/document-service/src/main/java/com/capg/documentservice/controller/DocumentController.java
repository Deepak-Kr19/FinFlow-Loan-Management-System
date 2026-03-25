package com.capg.documentservice.controller;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.uploadDocument(applicationId, type, file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<Document> verifyDocument(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        return ResponseEntity.ok(service.verifyDocument(id, status));
    }
}
