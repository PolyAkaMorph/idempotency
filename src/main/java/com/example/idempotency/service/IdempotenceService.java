package com.example.idempotency.service;

import com.example.idempotency.exception.RequestRunningException;
import com.example.idempotency.exception.ValidationException;
import com.example.idempotency.repo.DBCache;
import com.example.idempotency.repo.MemCache;
import com.example.idempotency.repo.entity.RequestEntity;
import com.example.idempotency.controller.model.Model;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;

import static com.example.idempotency.controller.model.Model.convertToModel;

@Service
@AllArgsConstructor
@Slf4j
public class IdempotenceService {
    private DBCache cache;
    private MemCache memCache;
    private ExternalService externalService;
    private FingerprintService fingerprintService;
    private CryptoService cryptoService;

    public Model getModel(String key, String id, String payload, String path, String method) throws RequestRunningException, ValidationException, GeneralSecurityException {
        Optional<RequestEntity> modelFromCache = cache.findAllByIdempotencyKey(key);
        String requestFingerprint = fingerprintService.getFingerprint(payload);

        if (modelFromCache.isEmpty()) {                 // cache miss
            log.info("cache miss");
            RequestEntity requestEntity = RequestEntity.builder()
                                        .idempotencyKey(key)
                                        //.externalId(id)
                                        .requestFingerprint(requestFingerprint)
                                        .requestMethod(method)
                                        .requestPath(path)
                    .build();

            cache.save(requestEntity);

            String data = externalService.process(id, payload);
            //requestEntity.setData(data);
            requestEntity.setProcessing(false);

            cache.save(getEncryptedCopy(requestEntity));

            return convertToModel(requestEntity);
        } else {                                        // cache hit
            log.info("cache hit");
            RequestEntity decryptedCopy = getDecryptedCopy(modelFromCache.get());
            if (decryptedCopy.isProcessing()) {
                throw new RequestRunningException();
            }
            validateFingerprint(requestFingerprint, decryptedCopy);
            validateMethod(decryptedCopy, path, method);
            return convertToModel(decryptedCopy);
        }
    }

    public Model getModelFromMemCache(String key, String id, String payload, String path, String method) throws RequestRunningException, ValidationException {
        Optional<RequestEntity> modelFromCache = memCache.getModel(key);
        String requestFingerprint = fingerprintService.getFingerprint(payload);

        if (modelFromCache.isEmpty()) {                 // cache miss
            RequestEntity requestEntity = RequestEntity.builder()
                    .idempotencyKey(key)
                    //.externalId(id)
                    .requestFingerprint(requestFingerprint)
                    .requestMethod(method)
                    .requestPath(path).build();



            if (!memCache.addModel(requestEntity)) { //todo discuss saving failure because of concurrency
                RequestEntity newModel = memCache.getModel(key)
                        .orElseThrow(IllegalStateException::new); // can't write, can't read
                if (newModel.isProcessing()) {
                    throw new RequestRunningException();
                } else {
                    validateFingerprint(requestFingerprint, newModel);
                    return convertToModel(newModel);
                }
            }

            String data = externalService.process(id, payload);
            //requestEntity.setData(data);
            requestEntity.setProcessing(false);
            if (!memCache.addModel(requestEntity)) {
                //todo discuss - ignore saving? in this case current request will be in processing forever
            }
            return convertToModel(requestEntity);
        } else {                                        // cache hit
            if (modelFromCache.get().isProcessing()) {
                throw new RequestRunningException();
            }
            validateFingerprint(requestFingerprint, modelFromCache.get());
            validateMethod(modelFromCache.get(), path, method);
            return convertToModel(modelFromCache.get());
        }
    }

    private void validateFingerprint(String requestFingerprint, RequestEntity model) throws ValidationException {
        String cacheFingerprint = model.getRequestFingerprint();
        if (!Objects.equals(cacheFingerprint, requestFingerprint)) {
            throw new ValidationException("Different payload");
        }
    }

    private void validateMethod(RequestEntity model, String path, String method) throws ValidationException {
        if (!Objects.equals(model.getRequestMethod(), method) || !Objects.equals(model.getRequestPath(), path)) {
            throw new ValidationException("Different path or method");
        }
    }

    private RequestEntity getEncryptedCopy(RequestEntity source) throws GeneralSecurityException {
        RequestEntity target = new RequestEntity();
        BeanUtils.copyProperties(source, target);
        String encryptedData = cryptoService.encrypt(target.getResponseData());
        target.setResponseData(encryptedData);
        return target;
    }

    private RequestEntity getDecryptedCopy(RequestEntity source) throws GeneralSecurityException {
        RequestEntity target = new RequestEntity();
        BeanUtils.copyProperties(source, target);
        String encryptedData = cryptoService.decrypt(target.getResponseData());
        target.setResponseData(encryptedData);
        return target;
    }
}
