package com.devvinnii.clientmanager.api.controller;

import com.devvinnii.clientmanager.api.dto.ClassGroupPageResponseDTO;
import com.devvinnii.clientmanager.api.dto.ClassGroupRequestDTO;
import com.devvinnii.clientmanager.api.dto.ClassGroupResponseDTO;
import com.devvinnii.clientmanager.api.dto.ClassGroupStatusRequestDTO;
import com.devvinnii.clientmanager.api.dto.EnrollmentRequestDTO;
import com.devvinnii.clientmanager.api.dto.EnrollmentResponseDTO;
import com.devvinnii.clientmanager.api.mapper.ClassGroupMapper;
import com.devvinnii.clientmanager.api.mapper.EnrollmentMapper;
import com.devvinnii.clientmanager.api.model.ClassGroup;
import com.devvinnii.clientmanager.api.model.Enrollment;
import com.devvinnii.clientmanager.api.model.Modality;
import com.devvinnii.clientmanager.api.service.ClassGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class ClassGroupController {
    private final ClassGroupService service;
    private final ClassGroupMapper classGroupMapper;
    private final EnrollmentMapper enrollmentMapper;

    public ClassGroupController(ClassGroupService service, ClassGroupMapper classGroupMapper,
                                EnrollmentMapper enrollmentMapper) {
        this.service = service;
        this.classGroupMapper = classGroupMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @PostMapping
    public ResponseEntity<ClassGroupResponseDTO> create(@Valid @RequestBody ClassGroupRequestDTO request) {
        ClassGroup created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(classGroupMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<ClassGroupPageResponseDTO> getPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Modality modality,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return ResponseEntity.ok(classGroupMapper.toPageResponse(
                service.getPage(name, modality, active, page, size, sortBy, sortDirection), sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classGroupMapper.toResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody ClassGroupRequestDTO request) {
        return ResponseEntity.ok(classGroupMapper.toResponse(service.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassGroupResponseDTO> updateStatus(@PathVariable Long id,
                                                               @Valid @RequestBody ClassGroupStatusRequestDTO request) {
        return ResponseEntity.ok(classGroupMapper.toResponse(service.updateStatus(id, request.active())));
    }

    @GetMapping("/{id}/alunos")
    public ResponseEntity<List<EnrollmentResponseDTO>> getActiveStudents(@PathVariable Long id) {
        return ResponseEntity.ok(service.getActiveStudents(id).stream().map(enrollmentMapper::toResponse).toList());
    }

    @PostMapping("/{id}/matriculas")
    public ResponseEntity<EnrollmentResponseDTO> enroll(@PathVariable Long id,
                                                         @Valid @RequestBody EnrollmentRequestDTO request) {
        Enrollment created = service.enroll(id, request.studentId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{enrollmentId}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(enrollmentMapper.toResponse(created));
    }

    @GetMapping("/{id}/matriculas")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollments(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEnrollments(id).stream().map(enrollmentMapper::toResponse).toList());
    }

    @DeleteMapping("/{id}/matriculas/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDTO> cancel(@PathVariable Long id, @PathVariable Long enrollmentId) {
        return ResponseEntity.ok(enrollmentMapper.toResponse(service.cancelEnrollment(id, enrollmentId)));
    }

    @PostMapping("/{id}/matriculas/{enrollmentId}/rematricula")
    public ResponseEntity<EnrollmentResponseDTO> reenroll(@PathVariable Long id, @PathVariable Long enrollmentId) {
        return ResponseEntity.status(201).body(enrollmentMapper.toResponse(service.reenroll(id, enrollmentId)));
    }

    @GetMapping("/alunos/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getStudentEnrollments(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getStudentEnrollments(studentId).stream().map(enrollmentMapper::toResponse).toList());
    }
}
