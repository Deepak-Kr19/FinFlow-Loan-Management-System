package com.capg.documentservice.service;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.repository.DocumentRepository;
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

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    @Value("${document.upload-dir:uploads/}")
    private String uploadDir;

    public Document uploadDocument(Long applicationId, String type, MultipartFile file) throws IOException {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());

        Document doc = new Document();
        doc.setApplicationId(applicationId);
        doc.setType(type);
        doc.setFilePath(filePath.toString());
        doc.setStatus("PENDING");

        return repository.save(doc);
    }

    public Document verifyDocument(Long documentId, String status) {
        Document doc = repository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        doc.setStatus(status); // VERIFIED or REJECTED
        return repository.save(doc);
    }
}
