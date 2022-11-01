package com.example.idempotency.repo;

import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.CryptoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@AllArgsConstructor
public class MemCache implements Cache {
    private CryptoService cryptoService;

    private Map<String, ModelEntity> memRepo = new ConcurrentHashMap<>();

    @Override
    public Optional<ModelEntity> getModel(String key) {
        log.info("Cache check, key = {}, models count = {}", key, memRepo.size());
        Optional<ModelEntity> modelEntity = Optional.ofNullable(memRepo.get(key));
        if (modelEntity.isPresent()) {
            ModelEntity model = new ModelEntity();
            BeanUtils.copyProperties(modelEntity.get(), model);
            try {
                String decryptedData = cryptoService.decrypt(model.getData());
                model.setData(decryptedData);
                return Optional.of(model);
            } catch (GeneralSecurityException e) {
                log.error("GeneralSecurityException, something wrong with cryptoService");
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addModel(ModelEntity model) {
        ModelEntity modelToSave = new ModelEntity();
        BeanUtils.copyProperties(model, modelToSave);
        log.info("Cache write, key = {}, models count = {}, isProcessing = {}", modelToSave.getIdempotencyKey(), memRepo.size(), modelToSave.isProcessing());
        try {
            String encryptedData = cryptoService.encrypt(modelToSave.getData());
            modelToSave.setData(encryptedData);
        } catch (GeneralSecurityException e) {
            log.error("GeneralSecurityException, something wrong with cryptoService");
            return false;
        }
        memRepo.put(modelToSave.getIdempotencyKey(), modelToSave);
        return true;
    }
}
