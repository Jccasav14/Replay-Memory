package com.replay.timeline;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import com.replay.timeline.dto.TimelineMonthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<TimelineMonthResponse>> getTimeline(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal User currentUser) {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        TimelineMonthResponse response = timelineService.getTimeline(currentUser.getId(), targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
