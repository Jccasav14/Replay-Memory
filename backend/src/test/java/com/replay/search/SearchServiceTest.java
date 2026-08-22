package com.replay.search;

import com.replay.ai.GeminiClient;
import com.replay.memories.Memory;
import com.replay.memories.MemoryRepository;
import com.replay.memories.MemoryType;
import com.replay.memories.ProcessingStatus;
import com.replay.search.dto.SearchRequest;
import com.replay.search.dto.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private SearchService searchService;

    private final String userId = "user-123";
    private Memory memory;

    @BeforeEach
    void setUp() {
        memory = Memory.builder()
                .id("mem-1")
                .userId(userId)
                .title("Trip to Cartagena")
                .description("Walking through the old walled city and eating ceviche")
                .type(MemoryType.PHOTO)
                .occurredAt(Instant.now())
                .processingStatus(ProcessingStatus.PROCESSED)
                .build();
    }

    @Test
    @DisplayName("Should return answer and matched memories when results exist")
    void search_WithResults_ReturnsMatchedMemories() {
        SearchRequest request = new SearchRequest();
        request.setQuery("Cartagena vacation");
        request.setTopK(5);

        Page<Memory> page = new PageImpl<>(List.of(memory));
        when(memoryRepository.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        SearchResponse response = searchService.search(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getMatchedMemories()).hasSize(1);
        assertThat(response.getAnswer()).contains("1 related event(s)");
        assertThat(response.getExtractedEntities()).containsKey("query");
    }

    @Test
    @DisplayName("Should return fallback answer when no matching memories are found")
    void search_NoResults_ReturnsEmptyFallback() {
        SearchRequest request = new SearchRequest();
        request.setQuery("Non-existent memory");
        request.setTopK(5);

        Page<Memory> page = new PageImpl<>(List.of());
        when(memoryRepository.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        SearchResponse response = searchService.search(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getMatchedMemories()).isEmpty();
        assertThat(response.getAnswer()).contains("No recorded memories");
    }
}
