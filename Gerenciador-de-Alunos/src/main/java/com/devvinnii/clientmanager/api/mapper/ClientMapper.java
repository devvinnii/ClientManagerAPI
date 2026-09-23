package com.devvinnii.clientmanager.api.mapper;

import com.devvinnii.clientmanager.api.dto.ClientResponseDTO;
import com.devvinnii.clientmanager.api.model.Client;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientMapper {

    public ClientResponseDTO toResponse(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getCpf(),
                client.getPhone(),
                client.getPhotoUrl()
        );
    }

    public List<ClientResponseDTO> toResponseList(List<Client> clients) {
        return clients.stream()
                .map(this::toResponse)
                .toList();
    }
}
