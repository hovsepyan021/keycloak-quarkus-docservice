package com.example.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Setter
    private UUID id;
    private String title;
    private String content;
    private String tenantId;
}