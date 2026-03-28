package com.capg.documentservice.controller;

import com.capg.documentservice.entity.Document;
import com.capg.documentservice.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService service;

    private Document testDoc;

    @BeforeEach
    void setUp() {
        testDoc = new Document();
        testDoc.setId(1L);
        testDoc.setApplicationId(100L);
        testDoc.setType("ID_PROOF");
        testDoc.setFilePath("/uploads/test.pdf");
        testDoc.setStatus("PENDING");
    }

    @Test
    @DisplayName("POST /documents/upload — should upload document")
    void uploadDocument() throws Exception {
        when(service.uploadDocument(eq(100L), eq("ID_PROOF"), any())).thenReturn(testDoc);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("applicationId", "100")
                        .param("type", "ID_PROOF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("ID_PROOF"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /documents/{id}/verify — should verify document")
    void verifyDocument() throws Exception {
        testDoc.setStatus("VERIFIED");
        when(service.verifyDocument(1L, "VERIFIED")).thenReturn(testDoc);

        mockMvc.perform(put("/documents/1/verify")
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }
}
