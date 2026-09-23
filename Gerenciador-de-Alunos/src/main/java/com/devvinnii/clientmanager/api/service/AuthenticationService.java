package com.devvinnii.clientmanager.api.service;

import com.devvinnii.clientmanager.api.dto.AuthResponse;
import com.devvinnii.clientmanager.api.dto.AuthenticatedUserResponse;
import com.devvinnii.clientmanager.api.dto.LoginRequest;
import com.devvinnii.clientmanager.api.model.AppUser;
import com.devvinnii.clientmanager.api.repository.AppUserRepository;
import com.devvinnii.clientmanager.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, AppUserRepository userRepository,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        Set<String> roles = new TreeSet<>(principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .toList());
        String token = jwtService.generateToken(principal.getUsername(), roles.stream().toList());
        return new AuthResponse(token, jwtService.getExpirationMs(), principal.getUsername(), roles);
    }

    public AuthenticatedUserResponse authenticatedUser(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Set<String> roles = new TreeSet<>(user.getRoles().stream().map(Enum::name).toList());
        return new AuthenticatedUserResponse(user.getUsername(), roles);
    }
}
