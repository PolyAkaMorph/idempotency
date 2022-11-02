package com.example.idempotency.aop;


import com.example.idempotency.dto.RequestDTO;
import com.example.idempotency.service.IdempotenceV1Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class IdempotenceInterceptor implements HandlerInterceptor {
    private static final String KEY = "Idempotency-Key";

    private IdempotenceV1Service idempotenceService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isNotIdempotence(handler, request)) {
            return true;
        }

        log.info("Idempotence processing");

        URL url = new URL(request.getRequestURL().toString());

        RequestDTO requestDTO = RequestDTO.builder()
                .idempotencyKey(request.getHeader(KEY))
                .requestMethod(request.getMethod())
                .requestPath(url.getPath())
                .build();

        Optional<RequestDTO> dataFromCache = idempotenceService.getDataFromCache(requestDTO);
        if (dataFromCache.isEmpty()) {
            log.info("Cache miss, returning to method");
            return true;
        }
        RequestDTO data = dataFromCache.get();
        log.info("Cache hit");

        response.setCharacterEncoding("UTF-8");
        response.setStatus(Integer.parseInt(data.getResponseCode()));
        response.getWriter().write(data.getResponseData());

        return false;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    private boolean isNotIdempotence(Object handler, HttpServletRequest request) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        IdempotentRequest filter = handlerMethod.getMethod().getAnnotation(IdempotentRequest.class);
        return Objects.isNull(filter) || Objects.isNull(request.getHeader(KEY));
    }
}