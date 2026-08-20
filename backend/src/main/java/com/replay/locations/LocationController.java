package com.replay.locations;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import lombok.Data;
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
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Location>>> listLocations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(locationService.listLocations(currentUser.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Location>> createLocation(
            @RequestBody CreateLocationDto dto,
            @AuthenticationPrincipal User currentUser) {
        Location location = Location.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .address(dto.getAddress())
                .radiusMeters(dto.getRadiusMeters() > 0 ? dto.getRadiusMeters() : 150.0)
                .build();

        Location created = locationService.createLocation(location, dto.getLatitude(), dto.getLongitude(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Location created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Location>> getLocation(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(locationService.getLocation(id, currentUser.getId())));
    }

    @Data
    public static class CreateLocationDto {
        private String name;
        private String category;
        private String address;
        private Double latitude;
        private Double longitude;
        private double radiusMeters;
    }
}
