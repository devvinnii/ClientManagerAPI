package com.devvinnii.clientmanager.api.storage;

import com.devvinnii.clientmanager.api.exception.InvalidPhotoException;
import com.devvinnii.clientmanager.api.exception.PhotoStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class LocalPhotoStorage implements PhotoStorage {

    private static final Map<String, String> MIME_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png"
    );
    private static final long MAX_PIXELS = 20_000_000L;

    private final Path uploadDirectory;
    private final long maxFileSize;

    public LocalPhotoStorage(@Value("${file.upload-dir}") String uploadDirectory,
                             @Value("${file.upload.max-size:10MB}") org.springframework.util.unit.DataSize maxFileSize) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize.toBytes();
    }

    @Override
    public String store(MultipartFile photo) {
        validateBasicProperties(photo);
        String extension = extensionOf(photo.getOriginalFilename());
        String expectedMimeType = MIME_BY_EXTENSION.get(extension);
        validateDeclaredMimeType(photo.getContentType(), expectedMimeType);
        validateImageContent(photo, expectedMimeType);

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path target = resolveSafePath(storedFileName);
        try {
            Files.createDirectories(uploadDirectory);
            try (InputStream inputStream = photo.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedFileName;
        } catch (IOException ex) {
            throw new PhotoStorageException("Não foi possível armazenar a foto", ex);
        }
    }

    @Override
    public StoredPhoto load(String storedFileName) {
        String extension = extensionOfStoredFile(storedFileName);
        Path path = resolveSafePath(storedFileName);
        try {
            UrlResource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new InvalidPhotoException("Foto não encontrada");
            }
            return new StoredPhoto(resource, MIME_BY_EXTENSION.get(extension));
        } catch (MalformedURLException ex) {
            throw new PhotoStorageException("Não foi possível acessar a foto", ex);
        }
    }

    @Override
    public void delete(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafePath(storedFileName));
        } catch (IOException ex) {
            throw new PhotoStorageException("Não foi possível remover a foto", ex);
        }
    }

    private void validateBasicProperties(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new InvalidPhotoException("A foto enviada está vazia");
        }
        if (photo.getSize() > maxFileSize) {
            throw new InvalidPhotoException("A foto excede o tamanho máximo permitido");
        }
        extensionOf(photo.getOriginalFilename());
    }

    private void validateDeclaredMimeType(String declaredMimeType, String expectedMimeType) {
        if (declaredMimeType == null || !expectedMimeType.equalsIgnoreCase(declaredMimeType)) {
            throw new InvalidPhotoException("O tipo MIME da foto não corresponde à extensão permitida");
        }
    }

    private void validateImageContent(MultipartFile photo, String expectedMimeType) {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(photo.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new InvalidPhotoException("O conteúdo enviado não é uma imagem válida");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String detectedMimeType = mimeTypeForFormat(reader.getFormatName());
                if (!expectedMimeType.equals(detectedMimeType)) {
                    throw new InvalidPhotoException("O conteúdo da foto não corresponde ao tipo informado");
                }
                BufferedImage image = reader.read(0);
                long pixels = (long) image.getWidth() * image.getHeight();
                if (image.getWidth() <= 0 || image.getHeight() <= 0 || pixels > MAX_PIXELS) {
                    throw new InvalidPhotoException("As dimensões da foto não são permitidas");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException ex) {
            throw new InvalidPhotoException("O conteúdo enviado não é uma imagem válida");
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || originalFilename.contains("/") || originalFilename.contains("\\")
                || originalFilename.contains("..")) {
            throw new InvalidPhotoException("A foto deve possuir uma extensão válida");
        }
        String filename = originalFilename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 1 || dotIndex == filename.length() - 1) {
            throw new InvalidPhotoException("A foto deve possuir extensão PNG, JPG ou JPEG");
        }
        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!MIME_BY_EXTENSION.containsKey(extension)) {
            throw new InvalidPhotoException("A extensão da foto não é permitida");
        }
        return extension;
    }

    private String extensionOfStoredFile(String storedFileName) {
        requireSafeFileName(storedFileName);
        return extensionOf(storedFileName);
    }

    private String mimeTypeForFormat(String format) {
        return switch (format.toLowerCase(Locale.ROOT)) {
            case "jpeg", "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> null;
        };
    }

    private Path resolveSafePath(String storedFileName) {
        requireSafeFileName(storedFileName);
        Path resolved = uploadDirectory.resolve(storedFileName).normalize();
        if (!resolved.startsWith(uploadDirectory)) {
            throw new InvalidPhotoException("Nome de arquivo inválido");
        }
        return resolved;
    }

    private void requireSafeFileName(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()
                || storedFileName.contains("/") || storedFileName.contains("\\")
                || storedFileName.contains("..") || !Path.of(storedFileName).getFileName().toString().equals(storedFileName)) {
            throw new InvalidPhotoException("Nome de arquivo inválido");
        }
    }
}
