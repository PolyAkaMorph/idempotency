package com.example.idempotency.service;

import org.springframework.stereotype.Service;

@Service
public class FingerprintService {
    public String getFingerprint(String payload) {
        //todo add hash
        return payload;
    }
}
