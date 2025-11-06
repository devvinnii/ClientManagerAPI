package com.devvinnii.clientmanager.api.service;

import com.devvinnii.clientmanager.api.dto.ClientDTO;
import com.devvinnii.clientmanager.api.exception.ClientAlreadyExistsException;
import com.devvinnii.clientmanager.api.exception.ClientNotFoundException;
import com.devvinnii.clientmanager.api.model.Client;
import com.devvinnii.clientmanager.api.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repository;

    // Diretório de upload vem do application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ======================
    // MÉTODOS CRUD PRINCIPAIS
    // ======================

    /**
     * Cria um novo cliente, validando CPF duplicado e salvando a foto (se existir).
     */
    public Client create(ClientDTO dto, MultipartFile photo) throws IOException {
        // Verifica se CPF já existe
        if (repository.findByCpf(dto.getCpf()).isPresent()) {
            throw new ClientAlreadyExistsException("Já existe um cliente cadastrado com o CPF: " + dto.getCpf());
        }

        // Salva a foto (se houver)
        String fileName = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                fileName = saveFile(photo);
            } catch (IOException e) {
                throw new FileUploadException("Erro ao salvar a foto do cliente", e);
            }
        }

        // Cria o objeto Client
        Client client = Client.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .phone(dto.getPhone())
                .photoUrl(fileName)
                .build();

        return repository.save(client);
    }

    /**
     * Retorna todos os clientes cadastrados.
     */
    public List<Client> getAll() {
        return repository.findAll();
    }

    /**
     * Busca um cliente pelo ID, ou lança exceção se não existir.
     */
    public Client getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente com ID " + id + " não encontrado"));
    }

    /**
     * Deleta um cliente existente, removendo também a foto se houver.
     */
    public void delete(Long id) {
        Client client = getById(id);

        // Exclui a foto do diretório local, se existir
        if (client.getPhotoUrl() != null) {
            File file = new File(uploadDir + File.separator + client.getPhotoUrl());
            if (file.exists()) {
                file.delete();
            }
        }

        repository.delete(client);
    }

    /**
     * Atualiza um cliente existente, substituindo a foto se enviada.
     */
    public Client update(Long id, ClientDTO dto, MultipartFile photo) throws IOException {
        Client existing = getById(id);

        // Atualiza dados
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        // Atualiza CPF se for diferente e ainda não existir
        if (!existing.getCpf().equals(dto.getCpf())) {
            if (repository.findByCpf(dto.getCpf()).isPresent()) {
                throw new ClientAlreadyExistsException("Já existe um cliente com o CPF " + dto.getCpf());
            }
            existing.setCpf(dto.getCpf());
        }

        // Atualiza a foto se enviada
        if (photo != null && !photo.isEmpty()) {
            // Apaga a antiga
            if (existing.getPhotoUrl() != null) {
                File old = new File(uploadDir + File.separator + existing.getPhotoUrl());
                if (old.exists()) old.delete();
            }

            try {
                String newFile = saveFile(photo);
                existing.setPhotoUrl(newFile);
            } catch (IOException e) {
                throw new FileUploadException("Erro ao atualizar a foto do cliente", e);
            }
        }

        return repository.save(existing);
    }

    // ======================
    // MÉTODO UTILITÁRIO INTERNO
    // ======================

    /**
     * Salva o arquivo localmente no diretório configurado.
     */
    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FileUploadException("Arquivo vazio não pode ser salvo");
        }

        // Cria o diretório se não existir
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Define o nome único do arquivo
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // Copia o conteúdo do arquivo
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }
}
