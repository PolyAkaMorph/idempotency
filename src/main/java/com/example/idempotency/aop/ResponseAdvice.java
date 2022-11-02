package com.example.idempotency.aop;

import com.example.idempotency.controller.model.Model;
import com.example.idempotency.service.IdempotenceV1Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;


@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final String KEY = "idempotency-key";

    private IdempotenceV1Service idempotenceService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //todo add checks for annotation
        if (!(body instanceof Model)) {
            return body;
        }
        HttpHeaders headers = request.getHeaders();
        if (headers.isEmpty() || CollectionUtils.isEmpty(headers.get(KEY))) {
            return body;
        }

        String idempotencyKey = headers.get(KEY).get(0);
        Model model = (Model) body;
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();

        int statusCode = servletResponse.getStatus();
        try {
            idempotenceService.setDataToCache(idempotencyKey, model.toString(), statusCode);
        } catch (GeneralSecurityException e) {
            //remove
            log.error("GeneralSecurityException");
        }
        log.info("Cache warm");
        return body;
    }
}
