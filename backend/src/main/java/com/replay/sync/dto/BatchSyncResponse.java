package com.replay.sync.dto;

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
public class BatchSyncResponse {

    private String status;
    private Instant processedAt;
    private List<IdMappingDto> mappings;
    private List<Object> serverChanges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdMappingDto {
        private String localId;
        private String remoteId;
        private String status; // SYNCED, CONFLICT_RESOLVED, FAILED
    }
}
