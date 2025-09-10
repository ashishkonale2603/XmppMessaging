package com.example.openfiremessaging.controller;

import com.example.openfiremessaging.dto.JwtResponse;
import com.example.openfiremessaging.dto.LoginRequest;
import com.example.openfiremessaging.security.JwtUtils;
import com.example.openfiremessaging.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        boolean isAuthenticated = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        if (isAuthenticated) {
            String jwt = jwtUtils.generateJwtToken(loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, loginRequest.getUsername()));
        } else {
            return ResponseEntity.status(401).body("Error: Invalid credentials");
        }
    }
}

