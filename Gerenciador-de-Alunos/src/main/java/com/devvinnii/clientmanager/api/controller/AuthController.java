package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.dto.AuthResponse;
import com.devvinnii.clientmanager.api.dto.AuthenticatedUserResponse;
import com.devvinnii.clientmanager.api.dto.LoginRequest;
import com.devvinnii.clientmanager.api.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> authenticatedUser(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(authenticationService.authenticatedUser(user.getUsername()));
    }
}
