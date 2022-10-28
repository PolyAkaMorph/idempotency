package com.example.idempotency.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class ExternalService {
    private final static Collection<String> processing = Collections.synchronizedCollection(new ArrayList<>());

    public String processId(String id) {
        if (processing.contains(id)) {
            return null; //todo 409 Conflict already processing
        } else {
            processing.add(id);
        }
        //todo if something shoots here, send 503?
        try {
            TimeUnit.SECONDS.sleep(1); // emulate load
        } catch (InterruptedException ignored) {
        } finally {
            processing.remove(id);
        }

        return RandomStringUtils.randomAlphabetic(10);
    }
}
