package com.example.idempotency.controller;

import com.example.idempotency.aop.IdempotentRequest;
import com.example.idempotency.service.ExternalService;
import com.example.idempotency.controller.model.Model;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@AllArgsConstructor
public class IdempotencyV1Controller {
    private ExternalService externalService;

    @PostMapping(value = "/v1/{id}")
    @ResponseBody
    @IdempotentRequest
    public Model postData(@PathVariable("id") String id, @RequestBody String payload) {
        log.info("Got request");
        String dataFromExternalService = externalService.process(id, payload);
        Model model = new Model(id, dataFromExternalService);
        return model;
    }

    @PutMapping(value = "/v1/{id}")
    @ResponseBody
    public Model getData(@PathVariable("id") String id, @RequestBody String payload) {
        String dataFromExternalService = externalService.process(id, payload);
        Model model = new Model(id, dataFromExternalService);
        return model;
    }
}
