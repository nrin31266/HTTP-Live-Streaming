package com.rin.hlsserver.controller;

import com.rin.hlsserver.dto.request.LoginRequest;
import com.rin.hlsserver.dto.request.RegisterRequest;
import com.rin.hlsserver.dto.response.ApiResponse;
import com.rin.hlsserver.dto.response.AuthResponse;
import com.rin.hlsserver.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        // Implementation for registration endpoint
        return ApiResponse.success(authService.registerUser(
            registerRequest.getEmail(),
            registerRequest.getPassword(),
            registerRequest.getFullName()
        ));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        // Implementation for login endpoint
        return ApiResponse.success(authService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

}
