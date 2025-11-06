package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.dto.ClientDTO;
import com.devvinnii.clientmanager.api.model.Client;
import com.devvinnii.clientmanager.api.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Client> create(
            @Valid @ModelAttribute ClientDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        Client created = service.create(dto, photo);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
