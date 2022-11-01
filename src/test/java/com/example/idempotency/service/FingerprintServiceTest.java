package com.example.idempotency.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

class FingerprintServiceTest {
    private final FingerprintService fingerprintService = new FingerprintService();

    private static final String MESSAGE = "MESSAGE";

    FingerprintServiceTest() throws NoSuchAlgorithmException {
    }

    @Test
    void getFingerprint() {
        String firstFingerprint = fingerprintService.getFingerprint(MESSAGE);
        String secondFingerprint = fingerprintService.getFingerprint(MESSAGE);
        Assertions.assertEquals(firstFingerprint, secondFingerprint);
    }
}