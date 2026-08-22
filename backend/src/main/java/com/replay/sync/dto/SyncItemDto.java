package com.replay.sync.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncItemDto {

    private String localId;
    private String entityType; // MEMORY, PERSON, LOCATION, OBJECT
    private String operation;  // INSERT, UPDATE, DELETE
    private Instant occurredAt;
    private JsonNode data;
}
