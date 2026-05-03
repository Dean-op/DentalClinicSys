package com.dentalclinic.controller;

import com.dentalclinic.common.ApiResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class UploadController {
    private final Path uploadDir;

    public UploadController(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ApiResponse<Map<String, String>> upload(@RequestParam MultipartFile file) throws IOException {
        Files.createDirectories(uploadDir);
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String suffix = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        Path target = uploadDir.resolve(UUID.randomUUID() + suffix);
        file.transferTo(target);
        return ApiResponse.ok(Map.of("path", "/uploads/" + target.getFileName()));
    }
}
