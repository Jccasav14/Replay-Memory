package com.replay.timeline;

import com.replay.memories.Memory;
import com.replay.memories.MemoryRepository;
import com.replay.memories.dto.MemoryResponse;
import com.replay.timeline.dto.TimelineDayGroup;
import com.replay.timeline.dto.TimelineMonthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final MemoryRepository memoryRepository;

    public TimelineMonthResponse getTimeline(String userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Instant start = yearMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = yearMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<Memory> memories = memoryRepository.findByUserIdAndOccurredAtBetween(userId, start, end);

        Map<LocalDate, List<MemoryResponse>> groupedByDay = new LinkedHashMap<>();

        for (Memory memory : memories) {
            LocalDate day = LocalDate.ofInstant(memory.getOccurredAt(), ZoneOffset.UTC);
            groupedByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(mapToResponse(memory));
        }

        List<TimelineDayGroup> dayGroups = new ArrayList<>();
        groupedByDay.forEach((date, dailyMemories) -> {
            dayGroups.add(TimelineDayGroup.builder()
                    .date(date.toString())
                    .memoryCount(dailyMemories.size())
                    .memories(dailyMemories)
                    .build());
        });

        return TimelineMonthResponse.builder()
                .year(year)
                .month(month)
                .totalMemories(memories.size())
                .days(dayGroups)
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
