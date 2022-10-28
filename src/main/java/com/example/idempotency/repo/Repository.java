package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.dto.Model;

import java.util.Optional;

public interface Repository {
    Optional<ModelEntity> getModel(String key);

    boolean addModel(ModelEntity model);
}
