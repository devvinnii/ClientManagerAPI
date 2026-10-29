package com.devvinnii.clientmanager.api.repository;

import com.devvinnii.clientmanager.api.model.ClassGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long>, JpaSpecificationExecutor<ClassGroup> {

    @EntityGraph(attributePaths = "schedules")
    @Query("select classGroup from ClassGroup classGroup where classGroup.id = :id")
    Optional<ClassGroup> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select classGroup from ClassGroup classGroup where classGroup.id = :id")
    Optional<ClassGroup> findByIdForUpdate(@Param("id") Long id);
}
