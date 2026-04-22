package com.intellidocs.intellidocs_ai.controller;

import com.intellidocs.intellidocs_ai.common.ApiResponse;
import com.intellidocs.intellidocs_ai.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(
                ApiResponse.ok("JWT working! userId=" + userId +
                        " tenantId=" + tenantId, null)
        );
    }
}