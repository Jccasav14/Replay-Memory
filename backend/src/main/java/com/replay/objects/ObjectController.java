package com.replay.objects;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ObjectEntity>>> listObjects(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(objectService.listObjects(currentUser.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ObjectEntity>> createObject(
            @RequestBody ObjectEntity object,
            @AuthenticationPrincipal User currentUser) {
        ObjectEntity created = objectService.createObject(object, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Object created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ObjectEntity>> getObject(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(objectService.getObject(id, currentUser.getId())));
    }
}
