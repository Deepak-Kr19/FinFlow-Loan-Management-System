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

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository repository;
    private final DocumentEventProducer eventProducer;

    public DocumentService(DocumentRepository repository, DocumentEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    @Value("${document.upload-dir:uploads/}")
    private String uploadDir;

    public Document uploadDocument(Long applicationId, String type, MultipartFile file) throws IOException {
        log.info("Uploading document: applicationId={}, type={}, fileName={}, size={}KB",
                applicationId, type, file.getOriginalFilename(), file.getSize() / 1024);

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
            log.debug("Created upload directory: {}", uploadDir);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());
        log.debug("File saved to: {}", filePath);

        Document doc = new Document();
        doc.setApplicationId(applicationId);
        doc.setType(type);
        doc.setFilePath(filePath.toString());
        doc.setStatus("PENDING");

        Document saved = repository.save(doc);
        log.info("Document saved: id={}, applicationId={}, type={}, status=PENDING", saved.getId(), applicationId, type);

        // Publish event to RabbitMQ
        DocumentEvent event = new DocumentEvent(saved.getId(), applicationId, type, "PENDING");
        eventProducer.publishDocumentUploaded(event);
        log.info("Published DOCUMENT_UPLOADED event for documentId={}", saved.getId());

        return saved;
    }

    public Document verifyDocument(Long documentId, String status) {
        log.info("Verifying document: id={}, newStatus={}", documentId, status);
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> {
                    log.warn("Document not found: id={}", documentId);
                    return new RuntimeException("Document not found");
                });
        String oldStatus = doc.getStatus();
        doc.setStatus(status);
        Document saved = repository.save(doc);
        log.info("Document verified: id={}, status changed from {} to {}", documentId, oldStatus, status);
        return saved;
    }
}
