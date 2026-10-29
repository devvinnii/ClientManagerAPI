package com.devvinnii.clientmanager.api.repository;


import com.devvinnii.clientmanager.api.model.Student;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByCpf(String cpf);

    List<Student> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select student from Student student where student.id = :id")
    Optional<Student> findByIdForEnrollment(@Param("id") Long id);
}
