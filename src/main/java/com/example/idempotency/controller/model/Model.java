package com.example.idempotency.controller.model;

import com.example.idempotency.repo.entity.RequestEntity;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Model {
    String id;
    String data;

    public static Model convertToModel(RequestEntity entity) {
        return new Model(entity.getId(), entity.getResponseData());
    }
}
