package com.capg.documentservice.service;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.event.DocumentEvent;
import com.capg.documentservice.event.DocumentEventProducer;
import com.capg.documentservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private DocumentEventProducer eventProducer;

    @InjectMocks
    private DocumentService service;

    @TempDir
    Path tempDir;

    private Document testDoc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString() + "/");

        testDoc = new Document();
        testDoc.setId(1L);
        testDoc.setApplicationId(100L);
        testDoc.setType("ID_PROOF");
        testDoc.setFilePath("/uploads/test.pdf");
        testDoc.setStatus("PENDING");
    }

    @Test
    @DisplayName("Upload — should save file, create document, and publish event")
    void uploadDocument_Success() throws IOException {
        when(repository.save(any(Document.class))).thenReturn(testDoc);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test-id.pdf", "application/pdf", "PDF content".getBytes());

        Document result = service.uploadDocument(100L, "ID_PROOF", file);

        assertNotNull(result);
        assertEquals("ID_PROOF", result.getType());
        assertEquals(100L, result.getApplicationId());
        verify(repository).save(any(Document.class));
        verify(eventProducer).publishDocumentUploaded(any(DocumentEvent.class));
    }

    @Test
    @DisplayName("Verify — should update document status to VERIFIED")
    void verifyDocument_Verified() {
        when(repository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(repository.save(any(Document.class))).thenReturn(testDoc);

        Document result = service.verifyDocument(1L, "VERIFIED");

        assertEquals("VERIFIED", testDoc.getStatus());
        verify(repository).save(testDoc);
    }

    @Test
    @DisplayName("Verify — should update document status to REJECTED")
    void verifyDocument_Rejected() {
        when(repository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(repository.save(any(Document.class))).thenReturn(testDoc);

        service.verifyDocument(1L, "REJECTED");

        assertEquals("REJECTED", testDoc.getStatus());
    }

    @Test
    @DisplayName("Verify — should throw when document not found")
    void verifyDocument_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.verifyDocument(99L, "VERIFIED"));
    }
}
