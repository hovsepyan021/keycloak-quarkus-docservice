package com.example.service;

import com.example.model.Document;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    DocumentService documentService = new DocumentService();

    @Test
    void testCreateAndFindById() {
        Document doc = new Document();
        doc.setTitle("Test Title");
        doc.setContent("Test Content");
        doc.setTenantId("tenant-1");

        Document created = documentService.create(doc);
        assertNotNull(created.getId());

        var found = documentService.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Title", found.get().getTitle());
        assertEquals("tenant-1", found.get().getTenantId());
    }
}
