package com.devvinnii.clientmanager.api.mapper;

import com.devvinnii.clientmanager.api.dto.StudentResponseDTO;
import com.devvinnii.clientmanager.api.dto.StudentPageResponseDTO;
import com.devvinnii.clientmanager.api.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentMapper {

    public StudentResponseDTO toResponse(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCpf(),
                student.getPhone(),
                student.getPhotoUrl(),
                student.isActive()
        );
    }

    public List<StudentResponseDTO> toResponseList(List<Student> students) {
        return students.stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentPageResponseDTO toPageResponse(Page<Student> students, String sortBy, String sortDirection) {
        return new StudentPageResponseDTO(
                students.getContent().stream().map(this::toResponse).toList(),
                students.getTotalElements(),
                students.getTotalPages(),
                students.getNumber(),
                students.getSize(),
                sortBy,
                sortDirection
        );
    }
}
