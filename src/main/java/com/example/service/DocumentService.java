package com.example.service;

import com.example.model.Document;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class DocumentService {

    private final Map<UUID, Document> store = new HashMap<>();

    public Document create(Document doc) {
        UUID id = UUID.randomUUID();
        doc.setId(id);
        store.put(id, doc);
        return doc;
    }

    public Optional<Document> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
