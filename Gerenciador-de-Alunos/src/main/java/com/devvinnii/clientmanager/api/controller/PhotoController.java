package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.storage.PhotoStorage;
import com.devvinnii.clientmanager.api.storage.StoredPhoto;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients/photos")
public class PhotoController {

    private final PhotoStorage photoStorage;

    public PhotoController(PhotoStorage photoStorage) {
        this.photoStorage = photoStorage;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String fileName) {
        StoredPhoto photo = photoStorage.load(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(photo.resource());
    }
}
