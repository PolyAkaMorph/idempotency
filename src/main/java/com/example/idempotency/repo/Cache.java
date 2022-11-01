package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;

import java.util.Optional;

public interface Cache {
    Optional<ModelEntity> getModel(String key);

    boolean addModel(ModelEntity model);
}
