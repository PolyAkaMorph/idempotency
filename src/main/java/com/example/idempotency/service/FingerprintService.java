package com.example.idempotency.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class FingerprintService {
    private final MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");

    public FingerprintService() throws NoSuchAlgorithmException {
    }

    public String getFingerprint(String payload) {
        return new String(messageDigest.digest(payload.getBytes()));
    }
}
