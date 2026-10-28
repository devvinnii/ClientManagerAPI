package com.devvinnii.clientmanager.api.repository;


import com.devvinnii.clientmanager.api.model.Student;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByCpf(String cpf);

    List<Student> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
