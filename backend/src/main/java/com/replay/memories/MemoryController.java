package com.replay.memories;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import com.replay.memories.dto.CreateMemoryRequest;
import com.replay.memories.dto.MemoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MemoryResponse>> createMemory(
            @Valid @RequestPart("data") CreateMemoryRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User currentUser) {
        MemoryResponse response = memoryService.createMemory(request, files, currentUser.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.created(response, "Memory created and queued for processing"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemoryResponse>>> listMemories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        Page<MemoryResponse> memories = memoryService.listMemories(currentUser.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(memories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemoryResponse>> getMemoryById(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        MemoryResponse memory = memoryService.getMemoryById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(memory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMemory(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        memoryService.deleteMemory(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Memory deleted successfully"));
    }
}
