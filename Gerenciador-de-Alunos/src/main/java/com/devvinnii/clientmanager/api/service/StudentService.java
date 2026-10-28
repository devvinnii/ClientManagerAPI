package com.devvinnii.clientmanager.api.service;

import com.devvinnii.clientmanager.api.dto.StudentRequestDTO;
import com.devvinnii.clientmanager.api.exception.InvalidStudentQueryException;
import com.devvinnii.clientmanager.api.exception.StudentAlreadyExistsException;
import com.devvinnii.clientmanager.api.exception.StudentNotFoundException;
import com.devvinnii.clientmanager.api.model.Student;
import com.devvinnii.clientmanager.api.repository.StudentRepository;
import com.devvinnii.clientmanager.api.repository.StudentSpecifications;
import com.devvinnii.clientmanager.api.storage.PhotoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "cpf", "email", "active");
    private final StudentRepository repository;
    private final PhotoStorage photoStorage;

    /**
     * Cria um novo aluno, validando CPF duplicado e salvando a foto (se existir).
     */
    @Transactional
    public Student create(StudentRequestDTO dto, MultipartFile photo) throws IOException {
        // Verifica se CPF já existe
        if (repository.findByCpf(dto.getCpf()).isPresent()) {
            throw new StudentAlreadyExistsException("Já existe um aluno cadastrado com o CPF: " + dto.getCpf());
        }

        // Salva a foto (se houver)
        String fileName = null;
        if (photo != null) {
            fileName = photoStorage.store(photo);
        }

        // Cria o objeto Student
        Student student = Student.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .phone(dto.getPhone())
                .photoUrl(fileName)
                .active(true)
                .build();

        try {
            return repository.save(student);
        } catch (RuntimeException ex) {
            photoStorage.delete(fileName);
            throw ex;
        }
    }

    /**
     * Retorna todos os alunos cadastrados.
     */
    public List<Student> getAll() {
        return repository.findAll();
    }

    public Page<Student> getPage(String name, String cpf, Boolean active, int page, int size,
                                 String sortBy, String sortDirection) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidStudentQueryException("Paginação inválida");
        }
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new InvalidStudentQueryException("Campo de ordenação inválido");
        }
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
                .orElseThrow(() -> new InvalidStudentQueryException("Direção de ordenação inválida"));
        return repository.findAll(StudentSpecifications.filteredBy(name, cpf, active),
                PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }

    /**
     * Busca um aluno pelo ID, ou lança exceção se não existir.
     */
    public Student getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Aluno com ID " + id + " não encontrado"));
    }

    /**
     * Deleta um aluno existente, removendo também a foto se houver.
     */
    @Transactional
    public String delete(Long id) {
        Student student = getById(id);

        String nome = student.getName();

        // Exclui a foto
        repository.delete(student);
        photoStorage.delete(student.getPhotoUrl());

        return nome;
    }

    /**
     * Atualiza um aluno existente, substituindo a foto se enviada.
     */
    @Transactional
    public Student update(Long id, StudentRequestDTO dto, MultipartFile photo) throws IOException {
        Student existing = getById(id);

        // Atualiza dados
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        // Atualiza CPF se for diferente e ainda não existir
        if (!existing.getCpf().equals(dto.getCpf())) {
            if (repository.findByCpf(dto.getCpf()).isPresent()) {
                throw new StudentAlreadyExistsException("Já existe um aluno com o CPF " + dto.getCpf());
            }
            existing.setCpf(dto.getCpf());
        }

        // Atualiza a foto se enviada
        if (photo != null) {
            String oldFile = existing.getPhotoUrl();
            String newFile = photoStorage.store(photo);
            existing.setPhotoUrl(newFile);
            try {
                Student updated = repository.save(existing);
                photoStorage.delete(oldFile);
                return updated;
            } catch (RuntimeException ex) {
                photoStorage.delete(newFile);
                throw ex;
            }
        }

        return repository.save(existing);
    }

    @Transactional
    public Student updateStatus(Long id, boolean active) {
        Student student = getById(id);
        student.setActive(active);
        return repository.save(student);
    }

    public Student getByCpf(String cpf) {
        return repository.findByCpf(cpf)
                .orElseThrow(() -> new StudentNotFoundException("Aluno com CPF " + cpf + " não encontrado"));
    }

    /**
     * Busca alunos cujo nome contenha o termo informado, sem diferenciar maiúsculas de minúsculas.
     */
    public List<Student> searchByName(String name) {
        return repository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
    }
}
