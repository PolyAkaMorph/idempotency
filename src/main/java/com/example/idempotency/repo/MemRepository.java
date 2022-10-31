package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.CryptoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@org.springframework.stereotype.Repository
@AllArgsConstructor
public class MemRepository implements Repository {
    private CryptoService cryptoService;

    private Map<String, ModelEntity> memRepo = new ConcurrentHashMap<>();

    @Override
    public Optional<ModelEntity> getModel(String key) {
        log.info("Cache check, key = {}, models count = {}", key, memRepo.size());
        Optional<ModelEntity> modelEntity = Optional.ofNullable(memRepo.get(key));
        if (modelEntity.isPresent()) {
            ModelEntity model = modelEntity.get();
            try {
                String decryptedData = cryptoService.decrypt(model.getData());
                model.setData(decryptedData);
                modelEntity = Optional.of(model);
            } catch (GeneralSecurityException e) {
                log.error("GeneralSecurityException, something wrong with cryptoService");
                return Optional.empty();
            }
        }

        return modelEntity;
    }

    @Override
    public boolean addModel(ModelEntity model) {
        //todo 422 Unprocessable Entity - expire policy (do we need it at the moment?)

        if (memRepo.containsKey(model.getKey())) {
            // can't be, in idempotency model values can't be updated
            return false;
        }

        log.info("Cache warm, key = {}, models count = {}", model.getKey(), memRepo.size());
        try {
            String encryptedData = cryptoService.encrypt(model.getData());
            model.setData(encryptedData);
        } catch (GeneralSecurityException e) {
            log.error("GeneralSecurityException, something wrong with cryptoService");
            return false;
        }
        memRepo.put(model.getKey(), model);
        return true;
    }
}
