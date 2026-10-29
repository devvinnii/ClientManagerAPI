package com.devvinnii.clientmanager.api.mapper;

import com.devvinnii.clientmanager.api.dto.ClassGroupPageResponseDTO;
import com.devvinnii.clientmanager.api.dto.ClassGroupResponseDTO;
import com.devvinnii.clientmanager.api.dto.ClassScheduleResponseDTO;
import com.devvinnii.clientmanager.api.model.ClassGroup;
import com.devvinnii.clientmanager.api.model.EnrollmentStatus;
import com.devvinnii.clientmanager.api.repository.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ClassGroupMapper {
    private final EnrollmentRepository enrollmentRepository;

    public ClassGroupMapper(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public ClassGroupResponseDTO toResponse(ClassGroup classGroup) {
        long occupied = enrollmentRepository.countByClassGroupIdAndStatus(classGroup.getId(), EnrollmentStatus.ATIVA);
        return new ClassGroupResponseDTO(
                classGroup.getId(),
                classGroup.getName(),
                classGroup.getModality(),
                classGroup.getDescription(),
                classGroup.getResponsible(),
                classGroup.getMaxCapacity(),
                occupied,
                Math.max(classGroup.getMaxCapacity() - occupied, 0),
                classGroup.isActive(),
                classGroup.getSchedules().stream().map(schedule -> new ClassScheduleResponseDTO(
                        schedule.getId(), schedule.getDayOfWeek(), schedule.getStartTime(), schedule.getEndTime())).toList(),
                classGroup.getCreatedAt(),
                classGroup.getUpdatedAt()
        );
    }

    public ClassGroupPageResponseDTO toPageResponse(Page<ClassGroup> page, String sortBy, String sortDirection) {
        return new ClassGroupPageResponseDTO(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize(), sortBy, sortDirection);
    }
}
