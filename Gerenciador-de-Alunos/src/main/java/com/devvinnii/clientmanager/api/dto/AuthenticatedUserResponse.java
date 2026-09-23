package com.devvinnii.clientmanager.api.dto;

import java.util.Set;

public record AuthenticatedUserResponse(String username, Set<String> roles) {
}
