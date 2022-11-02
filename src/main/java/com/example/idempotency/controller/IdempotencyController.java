package com.example.idempotency.controller;

import com.example.idempotency.aop.IdempotentRequest;
import com.example.idempotency.exception.RequestRunningException;
import com.example.idempotency.exception.ValidationException;
import com.example.idempotency.service.IdempotenceService;
import com.example.idempotency.controller.model.Model;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.util.Objects;

@Controller
@AllArgsConstructor
@Slf4j
public class IdempotencyController {
    private IdempotenceService idempotenceService;

    @PostMapping(value = "/get/{id}")
    @ResponseBody
    @IdempotentRequest
    public Model getTestData(@PathVariable("id") String id, @RequestHeader("Idempotency-Key") String key, @RequestBody String payload) {
        final String path = String.format("\\/get\\/%s", id);
        final String method = "POST";
        if (Objects.isNull(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty key");
        }
        Model model;
        try {
            model = idempotenceService.getModel(key, id, payload, path, method);
            //model = idempotenceService.getModelFromMemCache(key, id, payload, path, method);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (RequestRunningException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } //todo move to the filter
        return model;
    }
}
