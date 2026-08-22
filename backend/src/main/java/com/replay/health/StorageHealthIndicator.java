package com.replay.health;

import com.replay.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom Actuator Health Indicator checking local file storage directory access and free space.
 */
@Component
@RequiredArgsConstructor
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageProperties storageProperties;

    @Override
    public Health health() {
        try {
            File storageDir = new File(storageProperties.getRootPath());
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            if (!storageDir.canWrite()) {
                return Health.down()
                        .withDetail("storagePath", storageDir.getAbsolutePath())
                        .withDetail("error", "Directory is not writable")
                        .build();
            }

            long usableSpaceMb = storageDir.getUsableSpace() / (1024 * 1024);
            long totalSpaceMb = storageDir.getTotalSpace() / (1024 * 1024);

            return Health.up()
                    .withDetail("storagePath", storageDir.getAbsolutePath())
                    .withDetail("usableSpaceMb", usableSpaceMb)
                    .withDetail("totalSpaceMb", totalSpaceMb)
                    .withDetail("status", "READ_WRITE_OK")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
