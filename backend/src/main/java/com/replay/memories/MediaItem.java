package com.replay.memories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {

    private String mediaId;
    private String fileType; // IMAGE, VIDEO, DOCUMENT, AUDIO
    private String storagePath;
    private String thumbnailStoragePath;
    private String mimeType;
    private long fileSizeBytes;
    private String checksumSha256;
    private Map<String, Object> exifData;
}
