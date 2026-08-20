package com.replay.media;

import com.replay.common.BadRequestException;
import com.replay.common.ResourceNotFoundException;
import com.replay.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements FileStorageService {

    private final StorageProperties properties;
    private final Tika tika = new Tika();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(properties.getBasePath());
            Files.createDirectories(properties.getImagesPath());
            Files.createDirectories(properties.getThumbnailsPath());
            Files.createDirectories(properties.getDocumentsPath());
            Files.createDirectories(properties.getVideosPath());
            log.info("Initialized local file storage directories at: {}", properties.getBasePath().toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories", e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String userId) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename containing path traversal characters: " + originalFilename);
        }

        try {
            return store(file.getInputStream(), originalFilename, file.getContentType(), file.getSize(), userId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public StoredFile store(InputStream inputStream, String originalFilename, String declaredMimeType, long size, String userId) {
        String fileId = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        String targetFilename = fileId + (extension.isEmpty() ? "" : "." + extension);

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            DigestInputStream digestInputStream = new DigestInputStream(bufferedStream, sha256);

            // Detect actual MIME type using Tika (magic bytes)
            String detectedMimeType = tika.detect(bufferedStream, originalFilename);
            String category = determineCategory(detectedMimeType);
            Path targetDir = getTargetDirectory(category);

            Path destinationFile = targetDir.resolve(targetFilename).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(targetDir.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside target directory");
            }

            Files.copy(digestInputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            String checksum = HexFormat.of().formatHex(sha256.digest());

            // Generate thumbnail if image
            String thumbnailPath = null;
            if ("IMAGE".equals(category)) {
                thumbnailPath = generateThumbnail(destinationFile, fileId);
            }

            String relativeStoragePath = properties.getBasePath().relativize(destinationFile).toString().replace("\\", "/");

            log.info("Stored file: {} (MIME: {}, Category: {}, Size: {} bytes)", relativeStoragePath, detectedMimeType, category, size);

            return StoredFile.builder()
                    .fileId(fileId)
                    .originalFileName(originalFilename)
                    .storagePath(relativeStoragePath)
                    .thumbnailStoragePath(thumbnailPath)
                    .mimeType(detectedMimeType)
                    .fileSizeBytes(Files.size(destinationFile))
                    .checksumSha256(checksum)
                    .fileType(category)
                    .build();

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to store and process file", e);
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path file = properties.getBasePath().resolve(storagePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Could not read file: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Malformed file path: " + storagePath);
        }
    }

    @Override
    public Resource loadThumbnailAsResource(String thumbnailPath) {
        return loadAsResource(thumbnailPath);
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = properties.getBasePath().resolve(storagePath).normalize();
            Files.deleteIfExists(file);
            log.info("Deleted file: {}", storagePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", storagePath, e);
        }
    }

    private String generateThumbnail(Path sourceImage, String fileId) {
        try {
            String thumbFilename = "thumb_" + fileId + ".jpg";
            Path thumbTarget = properties.getThumbnailsPath().resolve(thumbFilename).normalize().toAbsolutePath();

            Thumbnails.of(sourceImage.toFile())
                    .size(250, 250)
                    .outputFormat("jpg")
                    .outputQuality(0.80)
                    .toFile(thumbTarget.toFile());

            return properties.getBasePath().relativize(thumbTarget).toString().replace("\\", "/");
        } catch (Exception e) {
            log.warn("Failed to generate thumbnail for {}: {}", sourceImage, e.getMessage());
            return null;
        }
    }

    private String determineCategory(String mimeType) {
        if (mimeType == null) return "DOCUMENT";
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        if (mimeType.startsWith("audio/")) return "AUDIO";
        return "DOCUMENT";
    }

    private Path getTargetDirectory(String category) {
        return switch (category) {
            case "IMAGE" -> properties.getImagesPath();
            case "VIDEO" -> properties.getVideosPath();
            case "AUDIO" -> properties.getBasePath().resolve("audio");
            default -> properties.getDocumentsPath();
        };
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}
