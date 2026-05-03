package com.capg.documentservice.service;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.event.DocumentEvent;
import com.capg.documentservice.event.DocumentEventProducer;
import com.capg.documentservice.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service layer for document management.
 * Handles file upload to the local filesystem and document verification.
 * On upload, publishes a DOCUMENT_UPLOADED event to RabbitMQ.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository repository;
    private final DocumentEventProducer eventProducer;

    public DocumentService(DocumentRepository repository, DocumentEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    /** Upload directory path, configurable via application.yml */
    @Value("${document.upload-dir:uploads/}")
    private String uploadDir;

    /**
     * Uploads a document file to the local filesystem and saves metadata to the database.
     * File is renamed with a UUID prefix to prevent naming conflicts.
     * Publishes a DOCUMENT_UPLOADED event to RabbitMQ after successful save.
     *
     * @param applicationId the loan application this document belongs to
     * @param type          document type (e.g., ID_PROOF, SALARY_SLIP)
     * @param file          the uploaded file (multipart)
     * @return the saved Document entity with file path and PENDING status
     * @throws IOException if file write fails
     */
    public Document uploadDocument(Long applicationId, String type, MultipartFile file) throws IOException {
        log.info("Uploading document: applicationId={}, type={}, fileName={}, size={}KB",
                applicationId, type, file.getOriginalFilename(), file.getSize() / 1024);

        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
            log.debug("Created upload directory: {}", uploadDir);
        }

        // Save file with UUID prefix to prevent naming conflicts
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());
        log.debug("File saved to: {}", filePath);

        // Save document metadata to database
        Document doc = new Document();
        doc.setApplicationId(applicationId);
        doc.setType(type);
        doc.setFilePath(filePath.toString());
        doc.setStatus("PENDING"); // Initial status — awaiting verification

        Document saved = repository.save(doc);
        log.info("Document saved: id={}, applicationId={}, type={}, status=PENDING", saved.getId(), applicationId, type);

        // Publish event to RabbitMQ for other services to react
        DocumentEvent event = new DocumentEvent(saved.getId(), applicationId, type, "PENDING");
        eventProducer.publishDocumentUploaded(event);
        log.info("Published DOCUMENT_UPLOADED event for documentId={}", saved.getId());

        return saved;
    }

    /**
     * Verifies or rejects a document (admin operation).
     * Updates the document status from PENDING to VERIFIED or REJECTED.
     *
     * @param documentId the document ID to verify
     * @param status     new status: "VERIFIED" or "REJECTED"
     * @return the updated Document entity
     * @throws RuntimeException if document not found
     */
    public Document verifyDocument(Long documentId, String status) {
        log.info("Verifying document: id={}, newStatus={}", documentId, status);
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> {
                    log.warn("Document not found: id={}", documentId);
                    return new RuntimeException("Document not found");
                });
        String oldStatus = doc.getStatus();
        doc.setStatus(status); // Update status: VERIFIED or REJECTED
        Document saved = repository.save(doc);
        log.info("Document verified: id={}, status changed from {} to {}", documentId, oldStatus, status);
        return saved;
    }

    /**
     * Fetches all documents belonging to a specific loan application.
     *
     * @param applicationId the loan application ID
     * @return list of documents for the application
     */
    public java.util.List<Document> getByApplicationId(Long applicationId) {
        log.info("Fetching documents for applicationId: {}", applicationId);
        return repository.findByApplicationId(applicationId);
    }

    /**
     * Fetches a single document by its ID.
     *
     * @param id the document ID
     * @return the Document entity
     * @throws RuntimeException if not found
     */
    public Document getDocumentById(Long id) {
        log.info("Fetching document by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document not found: id={}", id);
                    return new RuntimeException("Document not found");
                });
    }
}

