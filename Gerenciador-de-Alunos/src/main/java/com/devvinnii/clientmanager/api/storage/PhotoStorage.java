package com.devvinnii.clientmanager.api.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorage {
    String store(MultipartFile photo);

    StoredPhoto load(String storedFileName);

    void delete(String storedFileName);
}
