package com.intellidocs.intellidocs_ai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType; // always "Bearer"
    private long expiresIn; // in milliseconds
    private String userId;
    private String tenantId;
    private String role;
}
