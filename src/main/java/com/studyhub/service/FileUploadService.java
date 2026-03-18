package com.studyhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    @Value("${app.upload.dir}")
    private String uploadDir;

    public String uploadImage(MultipartFile file) {
        validate(file, IMAGE_TYPES);
        return save(file, "images");
    }

    public String uploadDocument(MultipartFile file) {
        validate(file, DOCUMENT_TYPES);
        return save(file, "documents");
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        Path path = Paths.get(uploadDir).toAbsolutePath()
                .resolve(fileUrl.replaceFirst("^/uploads/", ""));
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private void validate(MultipartFile file, Set<String> allowedTypes) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("No file provided.");
        if (file.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("File exceeds the 10 MB limit.");
        if (!allowedTypes.contains(file.getContentType()))
            throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
    }

    private String save(MultipartFile file, String subDir) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext != null ? "." + ext.toLowerCase() : "");
        Path dir = Paths.get(uploadDir).toAbsolutePath().resolve(subDir);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file.", e);
        }
        return "/uploads/" + subDir + "/" + filename;
    }
}
