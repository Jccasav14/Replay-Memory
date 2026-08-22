package com.replay.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.replay.memories.Memory;
import com.replay.memories.MemoryRepository;
import com.replay.memories.MemoryType;
import com.replay.memories.ProcessingStatus;
import com.replay.sync.dto.BatchSyncRequest;
import com.replay.sync.dto.BatchSyncResponse;
import com.replay.sync.dto.SyncItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final MemoryRepository memoryRepository;
    private final ObjectMapper objectMapper;

    public BatchSyncResponse processBatchSync(BatchSyncRequest request, String userId) {
        List<BatchSyncResponse.IdMappingDto> mappings = new ArrayList<>();

        if (request.getOperations() != null) {
            for (SyncItemDto op : request.getOperations()) {
                try {
                    if ("MEMORY".equalsIgnoreCase(op.getEntityType())) {
                        if ("INSERT".equalsIgnoreCase(op.getOperation())) {
                            Memory memory = Memory.builder()
                                    .userId(userId)
                                    .title(op.getData().has("title") ? op.getData().get("title").asText() : "Offline Memory")
                                    .description(op.getData().has("description") ? op.getData().get("description").asText() : "")
                                    .type(op.getData().has("type") ? MemoryType.valueOf(op.getData().get("type").asText()) : MemoryType.NOTE)
                                    .occurredAt(op.getOccurredAt() != null ? op.getOccurredAt() : Instant.now())
                                    .processingStatus(ProcessingStatus.PENDING_AI)
                                    .build();

                            Memory saved = memoryRepository.save(memory);
                            mappings.add(BatchSyncResponse.IdMappingDto.builder()
                                    .localId(op.getLocalId())
                                    .remoteId(saved.getId())
                                    .status("SYNCED")
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync item: {}", op.getLocalId(), e);
                    mappings.add(BatchSyncResponse.IdMappingDto.builder()
                            .localId(op.getLocalId())
                            .status("FAILED")
                            .build());
                }
            }
        }

        return BatchSyncResponse.builder()
                .status("SUCCESS")
                .processedAt(Instant.now())
                .mappings(mappings)
                .serverChanges(new ArrayList<>())
                .build();
    }
}
