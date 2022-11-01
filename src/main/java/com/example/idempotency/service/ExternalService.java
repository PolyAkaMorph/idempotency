package com.example.idempotency.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ExternalService {
    public String process(String id, String payload) {
        //todo process "answer" - 2xx and 4xx are ok
        try {
            TimeUnit.SECONDS.sleep(1); // emulate load
        } catch (InterruptedException ignored) {}

        return "Mock data for id = " + id + " " + RandomStringUtils.randomAlphabetic(10);
    }
}
