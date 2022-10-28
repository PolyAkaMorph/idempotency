package com.example.idempotency.service.dto;

import com.example.idempotency.repo.entity.ModelEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Model {
    String id;
    String data;

    public static Model convertToModel(ModelEntity entity) {
        return new Model(entity.getId(), entity.getData());
    }
}
