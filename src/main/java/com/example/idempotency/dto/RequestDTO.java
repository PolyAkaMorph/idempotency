package com.example.idempotency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@AllArgsConstructor
@Builder
public class RequestDTO {
    private String idempotencyKey;
    private String requestMethod;
    private String requestPath;
    private String requestFingerprint;
    private String requestBody;
    private String responseCode;
    private String responseData;
}
