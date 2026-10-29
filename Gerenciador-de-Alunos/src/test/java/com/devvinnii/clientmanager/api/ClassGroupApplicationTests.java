package com.devvinnii.clientmanager.api;

import com.devvinnii.clientmanager.api.dto.ClassGroupRequestDTO;
import com.devvinnii.clientmanager.api.dto.ClassScheduleRequestDTO;
import com.devvinnii.clientmanager.api.exception.ClassGroupConflictException;
import com.devvinnii.clientmanager.api.model.AppUser;
import com.devvinnii.clientmanager.api.model.ClassGroup;
import com.devvinnii.clientmanager.api.model.EnrollmentStatus;
import com.devvinnii.clientmanager.api.model.Modality;
import com.devvinnii.clientmanager.api.model.Role;
import com.devvinnii.clientmanager.api.model.Student;
import com.devvinnii.clientmanager.api.model.Weekday;
import com.devvinnii.clientmanager.api.repository.AppUserRepository;
import com.devvinnii.clientmanager.api.repository.ClassGroupRepository;
import com.devvinnii.clientmanager.api.repository.EnrollmentRepository;
import com.devvinnii.clientmanager.api.repository.StudentRepository;
import com.devvinnii.clientmanager.api.security.JwtService;
import com.devvinnii.clientmanager.api.service.ClassGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassGroupApplicationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ClassGroupService service;
    @Autowired ClassGroupRepository classGroupRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired AppUserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @AfterEach
    void cleanDatabase() {
        enrollmentRepository.deleteAll();
        classGroupRepository.deleteAll();
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @EnumSource(Modality.class)
    void createsClassGroupForEveryOfficialModality(Modality modality) throws Exception {
        createUser("admin", Role.ADMIN);

        mockMvc.perform(post("/api/turmas")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(modality, 12)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modality").value(modality.name()))
                .andExpect(jsonPath("$.availableSpots").value(12))
                .andExpect(jsonPath("$.schedules.length()").value(2));
    }

    @Test
    void rejectsInvalidModalityRequiredFieldsAndInvalidSchedules() throws Exception {
        createUser("admin", Role.ADMIN);
        String token = bearer("admin");

        mockMvc.perform(post("/api/turmas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(Modality.NATACAO, 10).replace("NATACAO", "FUTEBOL")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/turmas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"modality\":\"BALLET\",\"responsible\":\"\",\"maxCapacity\":0,\"schedules\":[]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.responsible").exists());

        mockMvc.perform(post("/api/turmas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(Modality.TRIATHLON, 10).replace("10:00", "08:00")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("O horário final deve ser posterior ao horário inicial"));
    }

    @Test
    void listsFiltersUpdatesAndChangesStatus() throws Exception {
        createUser("admin", Role.ADMIN);
        ClassGroup ballet = service.create(request("Ballet Juvenil", Modality.BALLET, 15));
        service.create(request("Natação Infantil", Modality.NATACAO, 10));

        mockMvc.perform(get("/api/turmas").header("Authorization", bearer("admin"))
                        .param("name", "Ballet").param("modality", "BALLET").param("active", "true")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ballet Juvenil"));

        mockMvc.perform(put("/api/turmas/" + ballet.getId()).header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson(Modality.BALLET, 20).replace("Turma Teste", "Ballet Avançado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ballet Avançado"));

        mockMvc.perform(patch("/api/turmas/" + ballet.getId() + "/status")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void protectsRoutesAndAllowsReadOnlyProfiles() throws Exception {
        createUser("funcionario", Role.FUNCIONARIO);
        service.create(request("Natação", Modality.NATACAO, 10));

        mockMvc.perform(get("/api/turmas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/turmas").header("Authorization", bearer("funcionario")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/turmas").header("Authorization", bearer("funcionario"))
                        .contentType(MediaType.APPLICATION_JSON).content(validRequestJson(Modality.BALLET, 10)))
                .andExpect(status().isForbidden());
    }

    @Test
    void enrollsCancelsAndReenrollsWhilePreservingHistory() throws Exception {
        createUser("admin", Role.ADMIN);
        Student student = saveStudent("Ana", "52998224725", true);
        ClassGroup classGroup = service.create(request("Natação A", Modality.NATACAO, 2));
        String token = bearer("admin");

        String enrollmentJson = mockMvc.perform(post("/api/turmas/" + classGroup.getId() + "/matriculas")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":" + student.getId() + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ATIVA"))
                .andReturn().getResponse().getContentAsString();
        Long enrollmentId = objectMapper.readTree(enrollmentJson).get("id").asLong();

        mockMvc.perform(post("/api/turmas/" + classGroup.getId() + "/matriculas")
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":" + student.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O aluno já possui matrícula ativa nesta turma"));

        mockMvc.perform(delete("/api/turmas/" + classGroup.getId() + "/matriculas/" + enrollmentId)
                        .header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(post("/api/turmas/" + classGroup.getId() + "/matriculas/" + enrollmentId + "/rematricula")
                        .header("Authorization", token))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ATIVA"));

        mockMvc.perform(get("/api/turmas/" + classGroup.getId() + "/alunos")
                        .header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].studentId").value(student.getId()))
                .andExpect(jsonPath("$[0].status").value("ATIVA"));

        mockMvc.perform(get("/api/turmas/alunos/" + student.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));

        assertThat(enrollmentRepository.findByClassGroupIdOrderByEnrolledAtDesc(classGroup.getId()))
                .hasSize(2)
                .extracting(enrollment -> enrollment.getStatus())
                .containsExactlyInAnyOrder(EnrollmentStatus.ATIVA, EnrollmentStatus.CANCELADA);
    }

    @Test
    void rejectsInactiveStudentsInactiveClassesAndFullClasses() throws Exception {
        Student inactive = saveStudent("Inativo", "11144477735", false);
        Student first = saveStudent("Primeiro", "12345678909", true);
        Student second = saveStudent("Segundo", "52998224725", true);
        ClassGroup classGroup = service.create(request("Triathlon", Modality.TRIATHLON, 1));

        org.junit.jupiter.api.Assertions.assertThrows(ClassGroupConflictException.class,
                () -> service.enroll(classGroup.getId(), inactive.getId()));
        service.enroll(classGroup.getId(), first.getId());
        org.junit.jupiter.api.Assertions.assertThrows(ClassGroupConflictException.class,
                () -> service.enroll(classGroup.getId(), second.getId()));
        service.updateStatus(classGroup.getId(), false);
        org.junit.jupiter.api.Assertions.assertThrows(ClassGroupConflictException.class,
                () -> service.enroll(classGroup.getId(), second.getId()));
    }

    @Test
    void doesNotReduceCapacityBelowCurrentOccupation() {
        Student student = saveStudent("Ana", "52998224725", true);
        ClassGroup classGroup = service.create(request("Ballet", Modality.BALLET, 2));
        service.enroll(classGroup.getId(), student.getId());

        ClassGroupRequestDTO zeroCapacity = request("Ballet", Modality.BALLET, 1);
        service.update(classGroup.getId(), zeroCapacity);
        ClassGroupRequestDTO invalidCapacity = new ClassGroupRequestDTO(
                "Ballet", Modality.BALLET, null, "Professora", 0, zeroCapacity.schedules());
        org.junit.jupiter.api.Assertions.assertThrows(ClassGroupConflictException.class,
                () -> service.update(classGroup.getId(), invalidCapacity));
    }

    @Test
    void serializesConcurrentEnrollmentForTheLastAvailableSpot() throws Exception {
        Student first = saveStudent("Ana", "52998224725", true);
        Student second = saveStudent("Bia", "11144477735", true);
        ClassGroup classGroup = service.create(request("Última vaga", Modality.NATACAO, 1));
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var completed = new CountDownLatch(2);
        var successes = new AtomicInteger();

        for (Long studentId : List.of(first.getId(), second.getId())) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    service.enroll(classGroup.getId(), studentId);
                    successes.incrementAndGet();
                } catch (ClassGroupConflictException ignored) {
                    // Uma das requisições deve perder a última vaga.
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    completed.countDown();
                }
            });
        }
        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(completed.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(enrollmentRepository.countByClassGroupIdAndStatus(classGroup.getId(), EnrollmentStatus.ATIVA))
                .isEqualTo(1);
    }

    private ClassGroupRequestDTO request(String name, Modality modality, int capacity) {
        return new ClassGroupRequestDTO(name, modality, "Descrição", "Professora Responsável", capacity, List.of(
                new ClassScheduleRequestDTO(Weekday.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new ClassScheduleRequestDTO(Weekday.QUARTA, LocalTime.of(9, 0), LocalTime.of(10, 0))));
    }

    private String validRequestJson(Modality modality, int capacity) throws Exception {
        return objectMapper.writeValueAsString(request("Turma Teste", modality, capacity));
    }

    private Student saveStudent(String name, String cpf, boolean active) {
        return studentRepository.save(Student.builder().name(name).cpf(cpf).active(active).build());
    }

    private void createUser(String username, Role role) {
        userRepository.save(AppUser.builder().username(username).passwordHash(passwordEncoder.encode("senha-segura"))
                .active(true).roles(Set.of(role)).build());
    }

    private String bearer(String username) {
        return "Bearer " + jwtService.generateToken(username, List.of());
    }
}
