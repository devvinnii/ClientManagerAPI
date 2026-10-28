package com.devvinnii.clientmanager.api.repository;

import com.devvinnii.clientmanager.api.model.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    public static Specification<Student> filteredBy(String name, String cpf, Boolean active) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (cpf != null && !cpf.isBlank()) {
                predicates.add(builder.equal(root.get("cpf"), cpf.trim()));
            }
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
