package com.devvinnii.clientmanager.api.repository;


import com.devvinnii.clientmanager.api.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCpf(String cpf);

    List<Client> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
