package com.intellidocs.intellidocs_ai.controller;

import com.intellidocs.intellidocs_ai.common.ApiResponse;
import com.intellidocs.intellidocs_ai.dto.request.LoginRequest;
import com.intellidocs.intellidocs_ai.dto.request.RegisterRequest;
import com.intellidocs.intellidocs_ai.dto.response.AuthResponse;
import com.intellidocs.intellidocs_ai.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 — resource created
                .body(ApiResponse.ok("Registration successful", authResponse));
    }



}
