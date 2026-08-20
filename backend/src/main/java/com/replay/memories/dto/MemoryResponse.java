package com.replay.memories.dto;

import com.replay.memories.AiAnalysis;
import com.replay.memories.LocationPoint;
import com.replay.memories.MediaItem;
import com.replay.memories.MemoryType;
import com.replay.memories.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryResponse {

    private String id;
    private String userId;
    private MemoryType type;
    private String title;
    private String description;
    private Instant occurredAt;
    private String timezone;
    private LocationPoint location;
    private List<MediaItem> media;
    private List<String> peopleIds;
    private List<String> objectIds;
    private List<String> tags;
    private AiAnalysis aiAnalysis;
    private ProcessingStatus processingStatus;
    private long syncVersion;
    private Instant createdAt;
    private Instant updatedAt;
}
