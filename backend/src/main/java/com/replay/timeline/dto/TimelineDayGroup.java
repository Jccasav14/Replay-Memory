package com.replay.timeline.dto;

import com.replay.memories.dto.MemoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDayGroup {

    private String date; // YYYY-MM-DD
    private int memoryCount;
    private List<MemoryResponse> memories;
}
