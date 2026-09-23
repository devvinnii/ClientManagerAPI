package com.devvinnii.clientmanager.api.dto;

import java.util.Set;

public record AuthResponse(String token, long expiresIn, String username, Set<String> roles) {
}
