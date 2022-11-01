package com.example.idempotency.service;

import com.example.idempotency.exception.RequestRunningException;
import com.example.idempotency.exception.ValidationException;
import com.example.idempotency.repo.DBCache;
import com.example.idempotency.repo.MemCache;
import com.example.idempotency.repo.entity.ModelEntity;
import com.example.idempotency.service.dto.Model;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;

import static com.example.idempotency.service.dto.Model.convertToModel;

@Service
@AllArgsConstructor
@Slf4j
public class IdempotenceService {
    private DBCache cache;
    private MemCache memCache;
    private ExternalService externalService;
    private FingerprintService fingerprintService;
    private CryptoService cryptoService;

    public Model getModel(String key, String id, String payload, String path, String method) throws RequestRunningException, ValidationException,
            GeneralSecurityException {
        Optional<ModelEntity> modelFromCache = cache.findAllByIdempotencyKey(key);
        String requestFingerprint = fingerprintService.getFingerprint(payload);

        if (modelFromCache.isEmpty()) {                 // cache miss
            ModelEntity modelEntity = ModelEntity.builder()
                                        .idempotencyKey(key)
                                        .externalId(id)
                                        .fingerprint(requestFingerprint)
                                        .method(method)
                                        .path(path).build();

            cache.save(modelEntity);

            String data = externalService.process(id, payload);
            modelEntity.setData(data);
            modelEntity.setProcessing(false);

            cache.save(getEncryptedCopy(modelEntity));

            return convertToModel(modelEntity);
        } else {                                        // cache hit
            ModelEntity decryptedCopy = getDecryptedCopy(modelFromCache.get());
            if (decryptedCopy.isProcessing()) {
                throw new RequestRunningException();
            }
            validateFingerprint(requestFingerprint, decryptedCopy);
            validateMethod(decryptedCopy, path, method);
            return convertToModel(decryptedCopy);
        }
    }

    public Model getModelFromMemCache(String key, String id, String payload, String path, String method) throws RequestRunningException, ValidationException {
        Optional<ModelEntity> modelFromCache = memCache.getModel(key);
        String requestFingerprint = fingerprintService.getFingerprint(payload);

        if (modelFromCache.isEmpty()) {                 // cache miss
            ModelEntity modelEntity = ModelEntity.builder()
                    .idempotencyKey(key)
                    .externalId(id)
                    .fingerprint(requestFingerprint)
                    .method(method)
                    .path(path).build();



            if (!memCache.addModel(modelEntity)) { //todo discuss saving failure because of concurrency
                ModelEntity newModel = memCache.getModel(key)
                        .orElseThrow(IllegalStateException::new); // can't write, can't read
                if (newModel.isProcessing()) {
                    throw new RequestRunningException();
                } else {
                    validateFingerprint(requestFingerprint, newModel);
                    return convertToModel(newModel);
                }
            }

            String data = externalService.process(id, payload);
            modelEntity.setData(data);
            modelEntity.setProcessing(false);
            if (!memCache.addModel(modelEntity)) {
                //todo discuss - ignore saving? in this case current request will be in processing forever
            }
            return convertToModel(modelEntity);
        } else {                                        // cache hit
            if (modelFromCache.get().isProcessing()) {
                throw new RequestRunningException();
            }
            validateFingerprint(requestFingerprint, modelFromCache.get());
            validateMethod(modelFromCache.get(), path, method);
            return convertToModel(modelFromCache.get());
        }
    }

    private void validateFingerprint(String requestFingerprint, ModelEntity model) throws ValidationException {
        String cacheFingerprint = model.getFingerprint();
        if (!Objects.equals(cacheFingerprint, requestFingerprint)) {
            throw new ValidationException("Different payload");
        }
    }

    private void validateMethod(ModelEntity model, String path, String method) throws ValidationException {
        if (!Objects.equals(model.getMethod(), method) || !Objects.equals(model.getPath(), path)) {
            throw new ValidationException("Different path or method");
        }
    }

    private ModelEntity getEncryptedCopy(ModelEntity source) throws GeneralSecurityException {
        ModelEntity target = new ModelEntity();
        BeanUtils.copyProperties(source, target);
        String encryptedData = cryptoService.encrypt(target.getData());
        target.setData(encryptedData);
        return target;
    }

    private ModelEntity getDecryptedCopy(ModelEntity source) throws GeneralSecurityException {
        ModelEntity target = new ModelEntity();
        BeanUtils.copyProperties(source, target);
        String encryptedData = cryptoService.decrypt(target.getData());
        target.setData(encryptedData);
        return target;
    }
}
