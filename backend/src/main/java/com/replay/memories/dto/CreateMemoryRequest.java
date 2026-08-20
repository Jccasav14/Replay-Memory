package com.replay.memories.dto;

import com.replay.memories.MemoryType;
import jakarta.validation.constraints.NotNull;
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
public class CreateMemoryRequest {

    @Builder.Default
    private MemoryType type = MemoryType.PHOTO;

    private String title;
    private String description;

    @NotNull(message = "occurredAt timestamp is required")
    private Instant occurredAt;

    private String timezone;
    private String locationName;
    private String locationAddress;
    private Double latitude;
    private Double longitude;
    private List<String> peopleIds;
    private List<String> objectIds;
    private List<String> tags;
}
