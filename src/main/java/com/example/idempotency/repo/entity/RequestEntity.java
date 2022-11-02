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
public class RequestEntity {
    @Id @Builder.Default private String id = UUID.randomUUID().toString();
    private String idempotencyKey;
    private String requestPath;
    private String requestMethod;
    private String requestFingerprint;
    private String responseCode;
    private String responseData;
    private boolean isProcessing;
    @Builder.Default private Long created = System.currentTimeMillis();
}