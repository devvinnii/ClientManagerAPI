package com.devvinnii.clientmanager.api.storage;

import org.springframework.core.io.Resource;

public record StoredPhoto(Resource resource, String contentType) {
}
