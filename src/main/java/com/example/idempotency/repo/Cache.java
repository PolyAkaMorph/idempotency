package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.RequestEntity;

import java.util.Optional;

public interface Cache {
    Optional<RequestEntity> getModel(String key);

    boolean addModel(RequestEntity model);
}
