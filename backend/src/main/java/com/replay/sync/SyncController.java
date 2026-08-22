package com.replay.sync;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import com.replay.sync.dto.BatchSyncRequest;
import com.replay.sync.dto.BatchSyncResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchSyncResponse>> batchSync(
            @RequestBody BatchSyncRequest request,
            @AuthenticationPrincipal User currentUser) {
        BatchSyncResponse response = syncService.processBatchSync(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Sync batch processed"));
    }
}
