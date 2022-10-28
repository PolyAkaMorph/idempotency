package com.example.idempotency.repo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelEntity {
    private String key;
    private String id;
    private String data;
    private String fingerprint;
}
