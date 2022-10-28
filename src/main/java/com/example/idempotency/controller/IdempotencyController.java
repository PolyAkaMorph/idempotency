package com.example.idempotency.controller;

import com.example.idempotency.service.dto.Model;
import com.example.idempotency.service.IdempotenceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@Controller
@AllArgsConstructor
public class IdempotencyController {
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    IdempotenceService idempotenceService;

    @GetMapping(value = "/get/{id}")
    public @ResponseBody Model getTestData(@PathVariable String id, @RequestParam("key") String key, @RequestParam("pl") String payload) throws Exception {
        //@RequestHeader("Idempotency-Key") String key, @RequestBody String payload
        //todo switch key to header, payload to body

        if (Objects.isNull(key)) {
            throw new Exception("Null key"); //todo 400 Bad Request https://datatracker.ietf.org/doc/html/draft-idempotency-header-00#section-2.7
        }

        Model model = idempotenceService.getModel(key, id, payload);
        //todo add transformer - exception to http code

        return model;
    }
}
