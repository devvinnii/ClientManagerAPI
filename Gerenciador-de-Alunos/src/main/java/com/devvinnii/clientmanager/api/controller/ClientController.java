package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.dto.ClientDTO;
import com.devvinnii.clientmanager.api.dto.ClientResponseDTO;
import com.devvinnii.clientmanager.api.mapper.ClientMapper;
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
    private final ClientMapper mapper;

    public ClientController(ClientService service, ClientMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAll() {
        return ResponseEntity.ok(mapper.toResponseList(service.getAll()));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ClientResponseDTO> create(
            @Valid @ModelAttribute ClientDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        Client created = service.create(dto, photo);
        return ResponseEntity.ok(mapper.toResponse(created));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<ClientResponseDTO> getByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(mapper.toResponse(service.getByCpf(cpf)));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<ClientResponseDTO>> searchByName(@PathVariable String name) {
        return ResponseEntity.ok(mapper.toResponseList(service.searchByName(name)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String nome = service.delete(id);
        return ResponseEntity.ok("O aluno " + nome + " foi excluído com sucesso!");

    }
}
