package com.replay.media;

import com.replay.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final FileStorageService storageService;
    private final Tika tika = new Tika();

    @GetMapping("/preview")
    public ResponseEntity<Resource> previewFile(@RequestParam("path") String storagePath) {
        Resource resource = storageService.loadAsResource(storagePath);
        String mimeType = "application/octet-stream";
        try {
            mimeType = tika.detect(resource.getInputStream(), resource.getFilename());
        } catch (IOException ignored) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }

    @GetMapping("/thumbnail")
    public ResponseEntity<Resource> previewThumbnail(@RequestParam("path") String thumbnailPath) {
        Resource resource = storageService.loadThumbnailAsResource(thumbnailPath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                .body(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String storagePath) {
        Resource resource = storageService.loadAsResource(storagePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
