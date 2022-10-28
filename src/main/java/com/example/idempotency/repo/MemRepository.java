package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.dto.Model;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@org.springframework.stereotype.Repository
public class MemRepository implements Repository {
    Map<String, ModelEntity> memRepo = new ConcurrentHashMap<>();

    @Override
    public Optional<ModelEntity> getModel(String key) {
        log.info("Cache check, key = {}, models count = {}", key, memRepo.size());
        return Optional.ofNullable(memRepo.get(key));
    }

    @Override
    public boolean addModel(ModelEntity model) {
        //todo add cryptography
        //todo 422 Unprocessable Entity - expire policy

        if (memRepo.containsKey(model.getKey())) {
            // can't be, in idempotency model values can't be updated
            return false;
        }

        log.info("Cache warm, key = {}, models count = {}", model.getKey(), memRepo.size());
        memRepo.put(model.getKey(), model);
        return true;
    }
}
