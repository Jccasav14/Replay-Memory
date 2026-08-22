package com.replay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "replay.storage")
public class StorageProperties {

    private String basePath = "./storage";
    private String imagesDir = "images";
    private String thumbnailsDir = "thumbnails";
    private String documentsDir = "documents";
    private String videosDir = "videos";
    private long maxFileSizeBytes = 52428800; // 50MB

    public Path getBasePath() {
        return Paths.get(basePath);
    }

    public Path getImagesPath() {
        return getBasePath().resolve(imagesDir);
    }

    public Path getThumbnailsPath() {
        return getBasePath().resolve(thumbnailsDir);
    }

    public Path getDocumentsPath() {
        return getBasePath().resolve(documentsDir);
    }

    public Path getVideosPath() {
        return getBasePath().resolve(videosDir);
    }
}
