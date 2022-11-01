package com.example.idempotency.repo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "modelEntity")
@Table(name = "cache")
@Builder
public class ModelEntity {
    @Id @Builder.Default private String id = UUID.randomUUID().toString();
    private String idempotencyKey;
    private String externalId;
    private String data;
    private String fingerprint;
    private String path;
    private String method;
    private boolean isProcessing;
}