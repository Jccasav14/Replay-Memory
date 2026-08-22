package com.replay.search;

import com.replay.ai.GeminiClient;
import com.replay.memories.Memory;
import com.replay.memories.MemoryRepository;
import com.replay.memories.dto.MemoryResponse;
import com.replay.search.dto.SearchRequest;
import com.replay.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemoryRepository memoryRepository;
    private final GeminiClient geminiClient;

    public SearchResponse search(SearchRequest request, String userId) {
        // Fetch candidates from memory repository (or Elasticsearch hybrid)
        List<Memory> candidateMemories = memoryRepository
                .findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(userId, PageRequest.of(0, request.getTopK()))
                .getContent();

        List<MemoryResponse> matched = candidateMemories.stream()
                .map(this::mapToResponse)
                .toList();

        String answer;
        if (matched.isEmpty()) {
            answer = "No recorded memories matching your query were found in your timeline.";
        } else {
            answer = "Based on your timeline, you have " + matched.size() + " related event(s) recorded.";
        }

        return SearchResponse.builder()
                .answer(answer)
                .matchedMemories(matched)
                .extractedEntities(Map.of("query", request.getQuery()))
                .build();
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
