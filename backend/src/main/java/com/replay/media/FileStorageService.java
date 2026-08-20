package com.replay.media;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    StoredFile store(MultipartFile file, String userId);

    StoredFile store(InputStream inputStream, String originalFilename, String mimeType, long size, String userId);

    Resource loadAsResource(String storagePath);

    Resource loadThumbnailAsResource(String thumbnailPath);

    void delete(String storagePath);
}
