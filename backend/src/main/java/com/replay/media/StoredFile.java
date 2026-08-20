package com.replay.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {

    private String fileId;
    private String originalFileName;
    private String storagePath;
    private String thumbnailStoragePath;
    private String mimeType;
    private long fileSizeBytes;
    private String checksumSha256;
    private String fileType; // IMAGE, VIDEO, DOCUMENT, AUDIO
}
