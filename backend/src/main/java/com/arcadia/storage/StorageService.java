package com.arcadia.storage;

import com.arcadia.common.exception.BadRequestException;
import com.arcadia.common.exception.InvalidGameArchiveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class StorageService {

    private static final long MAX_ZIP_ENTRIES = 1000;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path uploadDir;

    public StorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path gameDir(String slug) {
        return uploadDir.resolve("games").resolve(slug);
    }

    public long extractGameZip(MultipartFile file, Path targetDir) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new InvalidGameArchiveException("El archivo debe ser un ZIP (se recibió: " + originalName + ")");
        }
        try {
            Files.createDirectories(targetDir);
            boolean hasIndexHtml = false;
            long totalBytes = 0;
            int entries = 0;
            try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (++entries > MAX_ZIP_ENTRIES) {
                        throw new InvalidGameArchiveException("El ZIP contiene demasiados archivos");
                    }
                    if (entry.isDirectory()) {
                        continue;
                    }
                    Path out = targetDir.resolve(entry.getName()).normalize();
                    if (!out.startsWith(targetDir)) {
                        throw new InvalidGameArchiveException("El ZIP contiene rutas no válidas");
                    }
                    if (entry.getName().equals("index.html")) {
                        hasIndexHtml = true;
                    }
                    Files.createDirectories(out.getParent());
                    totalBytes += Files.copy(zip, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (!hasIndexHtml) {
                throw new InvalidGameArchiveException("El ZIP debe contener un index.html en su raíz");
            }
            return totalBytes;
        } catch (IOException e) {
            cleanupQuietly(targetDir);
            throw new InvalidGameArchiveException("No se pudo leer el ZIP: " + e.getMessage());
        } catch (InvalidGameArchiveException e) {
            cleanupQuietly(targetDir);
            throw e;
        }
    }

    private void cleanupQuietly(Path dir) {
        try {
            deleteDirectory(dir);
        } catch (RuntimeException ignored) {
        }
    }

    public String saveImage(MultipartFile file, Path targetDir, String baseName) {
        if (file.isEmpty()) {
            throw new BadRequestException("El archivo de imagen está vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen (se recibió: " + contentType + ")");
        }
        String extension = imageExtension(file.getOriginalFilename());
        String filename = baseName + "." + extension;
        try {
            Files.createDirectories(targetDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new BadRequestException("No se pudo guardar la imagen: " + e.getMessage());
        }
    }

    public void deleteDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new BadRequestException("No se pudo borrar " + path + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            throw new BadRequestException("No se pudo borrar el directorio " + dir + ": " + e.getMessage());
        }
    }

    private String imageExtension(String filename) {
        if (filename != null) {
            int dot = filename.lastIndexOf('.');
            if (dot >= 0) {
                String ext = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (IMAGE_EXTENSIONS.contains(ext)) {
                    return ext;
                }
            }
        }
        return "png";
    }
}
