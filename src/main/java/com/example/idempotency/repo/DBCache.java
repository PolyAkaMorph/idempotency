package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.RequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DBCache extends CrudRepository<RequestEntity, String> {
    Optional<RequestEntity> findAllByIdempotencyKey(String key);
}