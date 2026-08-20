package com.replay.locations;

import com.replay.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> listLocations(String userId) {
        return locationRepository.findByUserIdOrderByNameAsc(userId);
    }

    public Location createLocation(Location location, Double latitude, Double longitude, String userId) {
        location.setUserId(userId);
        if (latitude != null && longitude != null) {
            location.setGeoPoint(new GeoJsonPoint(longitude, latitude));
        }
        return locationRepository.save(location);
    }

    public Location getLocation(String id, String userId) {
        return locationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Location with ID " + id + " not found"));
    }
}
