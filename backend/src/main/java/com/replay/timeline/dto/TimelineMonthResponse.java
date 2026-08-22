package com.replay.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineMonthResponse {

    private int year;
    private int month;
    private int totalMemories;
    private List<TimelineDayGroup> days;
}
