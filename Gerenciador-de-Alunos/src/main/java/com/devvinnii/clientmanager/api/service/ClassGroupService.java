package com.devvinnii.clientmanager.api.service;

import com.devvinnii.clientmanager.api.dto.ClassGroupRequestDTO;
import com.devvinnii.clientmanager.api.dto.ClassScheduleRequestDTO;
import com.devvinnii.clientmanager.api.exception.ClassGroupConflictException;
import com.devvinnii.clientmanager.api.exception.ClassGroupNotFoundException;
import com.devvinnii.clientmanager.api.exception.EnrollmentNotFoundException;
import com.devvinnii.clientmanager.api.exception.InvalidClassGroupException;
import com.devvinnii.clientmanager.api.exception.StudentNotFoundException;
import com.devvinnii.clientmanager.api.model.ClassGroup;
import com.devvinnii.clientmanager.api.model.ClassSchedule;
import com.devvinnii.clientmanager.api.model.Enrollment;
import com.devvinnii.clientmanager.api.model.EnrollmentStatus;
import com.devvinnii.clientmanager.api.model.Modality;
import com.devvinnii.clientmanager.api.model.Student;
import com.devvinnii.clientmanager.api.repository.ClassGroupRepository;
import com.devvinnii.clientmanager.api.repository.ClassGroupSpecifications;
import com.devvinnii.clientmanager.api.repository.EnrollmentRepository;
import com.devvinnii.clientmanager.api.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassGroupService {
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "modality", "responsible", "maxCapacity", "active", "createdAt");

    private final ClassGroupRepository classGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public ClassGroup create(ClassGroupRequestDTO request) {
        validateSchedules(request.schedules());
        ClassGroup classGroup = new ClassGroup();
        applyRequest(classGroup, request);
        classGroup.setActive(true);
        return classGroupRepository.save(classGroup);
    }

    @Transactional(readOnly = true)
    public Page<ClassGroup> getPage(String name, Modality modality, Boolean active, int page, int size,
                                    String sortBy, String sortDirection) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidClassGroupException("Paginação inválida");
        }
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new InvalidClassGroupException("Campo de ordenação inválido");
        }
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
                .orElseThrow(() -> new InvalidClassGroupException("Direção de ordenação inválida"));
        return classGroupRepository.findAll(ClassGroupSpecifications.filteredBy(name, modality, active),
                PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }

    @Transactional(readOnly = true)
    public ClassGroup getById(Long id) {
        return classGroupRepository.findDetailedById(id)
                .orElseThrow(() -> new ClassGroupNotFoundException("Turma com ID " + id + " não encontrada"));
    }

    @Transactional
    public ClassGroup update(Long id, ClassGroupRequestDTO request) {
        validateSchedules(request.schedules());
        ClassGroup classGroup = lockClassGroup(id);
        long occupied = activeEnrollmentCount(id);
        if (request.maxCapacity() < occupied) {
            throw new ClassGroupConflictException("A capacidade não pode ser menor que as vagas ocupadas");
        }
        classGroup.clearSchedules();
        classGroupRepository.saveAndFlush(classGroup);
        applyRequest(classGroup, request);
        return classGroupRepository.save(classGroup);
    }

    @Transactional
    public ClassGroup updateStatus(Long id, boolean active) {
        ClassGroup classGroup = lockClassGroup(id);
        classGroup.setActive(active);
        return classGroupRepository.save(classGroup);
    }

    @Transactional
    public Enrollment enroll(Long classGroupId, Long studentId) {
        ClassGroup classGroup = lockClassGroup(classGroupId);
        if (!classGroup.isActive()) {
            throw new ClassGroupConflictException("Não é possível matricular aluno em uma turma inativa");
        }
        Student student = studentRepository.findByIdForEnrollment(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Aluno com ID " + studentId + " não encontrado"));
        if (!student.isActive()) {
            throw new ClassGroupConflictException("Não é possível matricular um aluno inativo");
        }
        if (enrollmentRepository.existsByClassGroupIdAndStudentIdAndStatus(classGroupId, studentId, EnrollmentStatus.ATIVA)) {
            throw new ClassGroupConflictException("O aluno já possui matrícula ativa nesta turma");
        }
        if (activeEnrollmentCount(classGroupId) >= classGroup.getMaxCapacity()) {
            throw new ClassGroupConflictException("A turma não possui vagas disponíveis");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setClassGroup(classGroup);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ATIVA);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment cancelEnrollment(Long classGroupId, Long enrollmentId) {
        lockClassGroup(classGroupId);
        Enrollment enrollment = enrollmentRepository.findByIdAndClassGroupId(enrollmentId, classGroupId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Matrícula não encontrada nesta turma"));
        if (enrollment.getStatus() == EnrollmentStatus.CANCELADA) {
            throw new ClassGroupConflictException("A matrícula já está cancelada");
        }
        enrollment.cancel();
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment reenroll(Long classGroupId, Long enrollmentId) {
        Enrollment previous = enrollmentRepository.findByIdAndClassGroupId(enrollmentId, classGroupId)
                .orElseThrow(() -> new EnrollmentNotFoundException("Matrícula não encontrada nesta turma"));
        if (previous.getStatus() != EnrollmentStatus.CANCELADA) {
            throw new ClassGroupConflictException("Somente matrículas canceladas podem ser refeitas");
        }
        return enroll(classGroupId, previous.getStudent().getId());
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollments(Long classGroupId) {
        requireClassGroup(classGroupId);
        return enrollmentRepository.findByClassGroupIdOrderByEnrolledAtDesc(classGroupId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getActiveStudents(Long classGroupId) {
        requireClassGroup(classGroupId);
        return enrollmentRepository.findByClassGroupIdAndStatusOrderByStudentNameAsc(classGroupId, EnrollmentStatus.ATIVA);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Aluno com ID " + studentId + " não encontrado");
        }
        return enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);
    }

    public long activeEnrollmentCount(Long classGroupId) {
        return enrollmentRepository.countByClassGroupIdAndStatus(classGroupId, EnrollmentStatus.ATIVA);
    }

    private void applyRequest(ClassGroup classGroup, ClassGroupRequestDTO request) {
        classGroup.setName(request.name().trim());
        classGroup.setModality(request.modality());
        classGroup.setDescription(normalizeOptional(request.description()));
        classGroup.setResponsible(request.responsible().trim());
        classGroup.setMaxCapacity(request.maxCapacity());
        classGroup.replaceSchedules(request.schedules().stream().map(this::toSchedule).toList());
    }

    private ClassSchedule toSchedule(ClassScheduleRequestDTO request) {
        ClassSchedule schedule = new ClassSchedule();
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        return schedule;
    }

    private void validateSchedules(List<ClassScheduleRequestDTO> schedules) {
        Set<ScheduleKey> uniqueSchedules = new HashSet<>();
        for (ClassScheduleRequestDTO schedule : schedules) {
            if (schedule.dayOfWeek() == null || schedule.startTime() == null || schedule.endTime() == null) {
                continue;
            }
            if (!schedule.endTime().isAfter(schedule.startTime())) {
                throw new InvalidClassGroupException("O horário final deve ser posterior ao horário inicial");
            }
            ScheduleKey key = new ScheduleKey(schedule.dayOfWeek().name(), schedule.startTime(), schedule.endTime());
            if (!uniqueSchedules.add(key)) {
                throw new InvalidClassGroupException("Não é permitido repetir o mesmo dia e intervalo de horário");
            }
        }
    }

    private ClassGroup lockClassGroup(Long id) {
        return classGroupRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ClassGroupNotFoundException("Turma com ID " + id + " não encontrada"));
    }

    private void requireClassGroup(Long id) {
        if (!classGroupRepository.existsById(id)) {
            throw new ClassGroupNotFoundException("Turma com ID " + id + " não encontrada");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ScheduleKey(String day, LocalTime start, LocalTime end) {
    }
}
