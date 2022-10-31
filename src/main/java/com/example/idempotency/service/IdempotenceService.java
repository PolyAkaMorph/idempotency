package com.example.idempotency.service;

import com.example.idempotency.repo.Repository;
import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.dto.Model;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.example.idempotency.service.dto.Model.convertToModel;

@Service
@AllArgsConstructor
public class IdempotenceService {
    private Repository repository;
    private ExternalService externalService;
    private FingerprintService fingerprintService;

    public Model getModel(String key, String id, String payload) {
        Optional<ModelEntity> modelFromCache = repository.getModel(key);
        String fingerprint = fingerprintService.getFingerprint(payload);

        if (modelFromCache.isEmpty()) {
            String data = externalService.processId(id);
            ModelEntity modelEntity = new ModelEntity(key, id, data, fingerprint);
            if(!repository.addModel(modelEntity)) {
                // recursion is ok? on another call modelFromCache will be not empty
                // possible in concurrent calls
                return getModel(key, id, payload);
            }
            return convertToModel(modelEntity);
        } else {
            if (Objects.equals(fingerprint, modelFromCache.get().getFingerprint())) {
                return convertToModel(modelFromCache.get());
            }
            return null; //todo 422 Unprocessable Entity - wrong fingerprint
        }

    }

}
