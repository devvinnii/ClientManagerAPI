package com.devvinnii.clientmanager.api.repository;

import com.devvinnii.clientmanager.api.model.ClassGroup;
import com.devvinnii.clientmanager.api.model.Modality;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public final class ClassGroupSpecifications {
    private ClassGroupSpecifications() {
    }

    public static Specification<ClassGroup> filteredBy(String name, Modality modality, Boolean active) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (name != null && !name.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (modality != null) {
                predicates.add(builder.equal(root.get("modality"), modality));
            }
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
