package com.example.idempotency.service;

import com.example.idempotency.dto.RequestDTO;
import com.example.idempotency.repo.DBCache;
import com.example.idempotency.repo.entity.RequestEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class IdempotenceV1Service {
    private DBCache dbCache;
    private CryptoService cryptoService;
    private FingerprintService fingerprintService;

    public Optional<RequestDTO> getDataFromCache(RequestDTO request) throws GeneralSecurityException {
        Optional<RequestEntity> fromCache = dbCache.findAllByIdempotencyKey(request.getIdempotencyKey());

        if (fromCache.isEmpty()) {
            RequestEntity requestEntity = RequestEntity.builder()
                    .idempotencyKey(request.getIdempotencyKey())
                    .requestPath(request.getRequestPath())
                    .requestMethod(request.getRequestMethod())
                    .requestFingerprint("")
                    .isProcessing(true)
                    .build();

            dbCache.save(requestEntity);

            return Optional.empty();
        }
        RequestDTO response = RequestDTO.builder()
                .responseCode(fromCache.get().getResponseCode())
                .responseData(cryptoService.decrypt(fromCache.get().getResponseData()))
                .build();
        return Optional.of(response);
    }

    public void setDataToCache(String idempotencyKey, String responseData, int statusCode) throws GeneralSecurityException {
        Optional<RequestEntity> fromCache = dbCache.findAllByIdempotencyKey(idempotencyKey);
        if (fromCache.isEmpty()) {
            throw new IllegalStateException(); //can't be
        }

        RequestEntity toSave = fromCache.get();
        toSave.setProcessing(false);
        toSave.setResponseData(cryptoService.encrypt(responseData));
        toSave.setResponseCode(String.valueOf(statusCode));
        dbCache.save(toSave);
    }

}
