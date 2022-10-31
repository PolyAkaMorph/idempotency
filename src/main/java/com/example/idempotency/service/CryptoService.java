package com.example.idempotency.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.stream.IntStream;

@Service
public class CryptoService {
    private static final String STATIC_SALT = "1234567890";
    private static final String TRANSFORMATION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

    private final Cipher decryptCipher;
    private final Cipher encryptCipher;

    public CryptoService(@Value("${crypto.key}") String key) {
        SecretKeyFactory factory = initSecretKeyFactory();
        KeySpec spec = new PBEKeySpec(key.toCharArray(), STATIC_SALT.getBytes(), 65536, 256);
        SecretKey secretKey = initSecretKeySpec(factory, spec);

        encryptCipher = initCipher(Cipher.ENCRYPT_MODE, secretKey);
        decryptCipher = initCipher(Cipher.DECRYPT_MODE, secretKey);
    }

    public String encrypt(String text) throws GeneralSecurityException {
        byte[] bytes = text.getBytes(); // StandardCharsets.UTF_8 ?
        byte[] encrypted;
        try {
            encrypted = encryptCipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Something wrong with text or key");
        }

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String text) throws GeneralSecurityException {
        byte[] bytes = Base64.getDecoder().decode(text.getBytes()); // StandardCharsets.UTF_8 ?
        byte[] decrypted;
        try {
            decrypted = decryptCipher.doFinal(bytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new GeneralSecurityException("Something wrong with text or key");
        }

        return new String(decrypted);
    }


    private static SecretKeyFactory initSecretKeyFactory() {
        try {
            return SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SecretKeyFactory exception");
        }
    }

    private static SecretKeySpec initSecretKeySpec(SecretKeyFactory factory, KeySpec spec) {
        try {
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("SecretKey exception");
        }
    }

    private static Cipher initCipher(int cryptMode, SecretKey secretKey) {
        byte[] iv = new byte[16];
        IntStream.range(0, iv.length).forEach((index) -> iv[index] = (byte) index);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM);
            cipher.init(cryptMode, secretKey, ivParameterSpec);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new IllegalStateException("Cipher exception");
        }
    }

}
