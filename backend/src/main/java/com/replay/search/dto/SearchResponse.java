package com.replay.search.dto;

import com.replay.memories.dto.MemoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String answer;
    private List<MemoryResponse> matchedMemories;
    private Map<String, Object> extractedEntities;
}
