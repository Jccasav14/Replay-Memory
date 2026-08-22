package com.replay.memories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPoint {

    private String locationId;
    private String name;
    private String address;
    private GeoJsonPoint geoPoint;
    private Double latitude;
    private Double longitude;
}
