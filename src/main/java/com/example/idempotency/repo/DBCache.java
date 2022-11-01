package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DBCache extends CrudRepository<ModelEntity, String> {
    Optional<ModelEntity> findAllByIdempotencyKey(String key);
}