package com.replay.memories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "memories")
@CompoundIndexes({
        @CompoundIndex(name = "user_occurred_idx", def = "{'userId': 1, 'occurredAt': -1}"),
        @CompoundIndex(name = "user_status_deleted_idx", def = "{'userId': 1, 'isDeleted': 1, 'processingStatus': 1}")
})
public class Memory {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Builder.Default
    private MemoryType type = MemoryType.PHOTO;

    private String title;

    private String description;

    @Indexed
    private Instant occurredAt;

    private String timezone;

    private LocationPoint location;

    @Builder.Default
    private List<MediaItem> media = new ArrayList<>();

    @Builder.Default
    private List<String> peopleIds = new ArrayList<>();

    @Builder.Default
    private List<String> objectIds = new ArrayList<>();

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private AiAnalysis aiAnalysis;

    private List<Double> embedding;

    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING_STORAGE;

    @Builder.Default
    private long syncVersion = 1L;

    @Builder.Default
    private boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
