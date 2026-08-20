package com.replay.memories;

import com.replay.common.ResourceNotFoundException;
import com.replay.media.FileStorageService;
import com.replay.media.StoredFile;
import com.replay.memories.dto.CreateMemoryRequest;
import com.replay.memories.dto.MemoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final FileStorageService storageService;

    public MemoryResponse createMemory(CreateMemoryRequest request, List<MultipartFile> files, String userId) {
        List<MediaItem> mediaItems = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    StoredFile storedFile = storageService.store(file, userId);
                    mediaItems.add(MediaItem.builder()
                            .mediaId(storedFile.getFileId())
                            .fileType(storedFile.getFileType())
                            .storagePath(storedFile.getStoragePath())
                            .thumbnailStoragePath(storedFile.getThumbnailStoragePath())
                            .mimeType(storedFile.getMimeType())
                            .fileSizeBytes(storedFile.getFileSizeBytes())
                            .checksumSha256(storedFile.getChecksumSha256())
                            .build());
                }
            }
        }

        LocationPoint locationPoint = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            locationPoint = LocationPoint.builder()
                    .name(request.getLocationName())
                    .address(request.getLocationAddress())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .geoPoint(new GeoJsonPoint(request.getLongitude(), request.getLatitude()))
                    .build();
        }

        Memory memory = Memory.builder()
                .userId(userId)
                .type(request.getType() != null ? request.getType() : (mediaItems.isEmpty() ? MemoryType.NOTE : MemoryType.PHOTO))
                .title(request.getTitle())
                .description(request.getDescription())
                .occurredAt(request.getOccurredAt())
                .timezone(request.getTimezone())
                .location(locationPoint)
                .media(mediaItems)
                .peopleIds(request.getPeopleIds() != null ? request.getPeopleIds() : new ArrayList<>())
                .objectIds(request.getObjectIds() != null ? request.getObjectIds() : new ArrayList<>())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .processingStatus(ProcessingStatus.PENDING_AI)
                .build();

        Memory savedMemory = memoryRepository.save(memory);
        log.info("Created memory ID: {} for user: {}", savedMemory.getId(), userId);

        return mapToResponse(savedMemory);
    }

    public Page<MemoryResponse> listMemories(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        return memoryRepository.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId, pageRequest)
                .map(this::mapToResponse);
    }

    public MemoryResponse getMemoryById(String id, String userId) {
        Memory memory = memoryRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Memory with ID " + id + " not found"));
        return mapToResponse(memory);
    }

    public void deleteMemory(String id, String userId) {
        Memory memory = memoryRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Memory with ID " + id + " not found"));

        memory.setDeleted(true);
        memoryRepository.save(memory);

        // Clean up binaries asynchronously or trigger lifecycle delete
        for (MediaItem item : memory.getMedia()) {
            if (item.getStoragePath() != null) {
                storageService.delete(item.getStoragePath());
            }
            if (item.getThumbnailStoragePath() != null) {
                storageService.delete(item.getThumbnailStoragePath());
            }
        }

        log.info("Soft-deleted memory ID: {} for user: {}", id, userId);
    }

    private MemoryResponse mapToResponse(Memory memory) {
        return MemoryResponse.builder()
                .id(memory.getId())
                .userId(memory.getUserId())
                .type(memory.getType())
                .title(memory.getTitle())
                .description(memory.getDescription())
                .occurredAt(memory.getOccurredAt())
                .timezone(memory.getTimezone())
                .location(memory.getLocation())
                .media(memory.getMedia())
                .peopleIds(memory.getPeopleIds())
                .objectIds(memory.getObjectIds())
                .tags(memory.getTags())
                .aiAnalysis(memory.getAiAnalysis())
                .processingStatus(memory.getProcessingStatus())
                .syncVersion(memory.getSyncVersion())
                .createdAt(memory.getCreatedAt())
                .updatedAt(memory.getUpdatedAt())
                .build();
    }
}
