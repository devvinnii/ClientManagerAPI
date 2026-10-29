package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.dto.StudentRequestDTO;
import com.devvinnii.clientmanager.api.dto.StudentResponseDTO;
import com.devvinnii.clientmanager.api.dto.StudentPageResponseDTO;
import com.devvinnii.clientmanager.api.dto.StudentStatusRequestDTO;
import com.devvinnii.clientmanager.api.mapper.StudentMapper;
import com.devvinnii.clientmanager.api.model.Student;
import com.devvinnii.clientmanager.api.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class StudentController {
    private final StudentService service;
    private final StudentMapper mapper;

    public StudentController(StudentService service, StudentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAll() {
        return ResponseEntity.ok(mapper.toResponseList(service.getAll()));
    }

    @GetMapping("/page")
    public ResponseEntity<StudentPageResponseDTO> getPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        return ResponseEntity.ok(mapper.toPageResponse(
                service.getPage(name, cpf, active, page, size, sortBy, sortDirection), sortBy, sortDirection));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<StudentResponseDTO> create(
            @Valid @ModelAttribute StudentRequestDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        Student created = service.create(dto, photo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<StudentResponseDTO> getByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(mapper.toResponse(service.getByCpf(cpf)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.getById(id)));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<StudentResponseDTO>> searchByName(@PathVariable String name) {
        return ResponseEntity.ok(mapper.toResponseList(service.searchByName(name)));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<StudentResponseDTO> update(
            @PathVariable Long id,
            @Valid @ModelAttribute StudentRequestDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        return ResponseEntity.ok(mapper.toResponse(service.update(id, dto, photo)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<StudentResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StudentStatusRequestDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(service.updateStatus(id, request.active())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String nome = service.delete(id);
        return ResponseEntity.ok("O aluno " + nome + " foi excluído com sucesso!");

    }
}
