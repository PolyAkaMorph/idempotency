package com.example.idempotency.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

class CryptoServiceTest {
    private static final String SECRET_KEY = "Curiouser and curiouser";
    private static final String DECRYPTED_MESSAGE = "message";
    private static final String ENCRYPTED_MESSAGE = "Loto7Sx8+/WNsXH6wLCtqA==";

    private CryptoService cryptoService = new CryptoService(SECRET_KEY);

    @Test
    void encrypt() throws GeneralSecurityException {
        String encrypted = cryptoService.encrypt(DECRYPTED_MESSAGE);

        Assertions.assertEquals(ENCRYPTED_MESSAGE, encrypted);
    }

    @Test
    void decrypt() throws GeneralSecurityException {
        String decrypted = cryptoService.decrypt(ENCRYPTED_MESSAGE);

        Assertions.assertEquals(DECRYPTED_MESSAGE, decrypted);
    }
}