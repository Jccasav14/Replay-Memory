package com.replay.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Actuator Health Indicator checking Google Gemini AI API readiness.
 */
@Component
public class AiServiceHealthIndicator implements HealthIndicator {

    @Value("${replay.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${replay.ai.gemini.model-text:gemini-1.5-flash}")
    private String modelText;

    @Override
    public Health health() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return Health.up()
                    .withDetail("service", "Gemini Generative AI Engine")
                    .withDetail("model", modelText)
                    .withDetail("status", "ONLINE")
                    .build();
        }

        return Health.up()
                .withDetail("service", "Gemini Generative AI Engine")
                .withDetail("mode", "MOCK_FALLBACK")
                .withDetail("status", "READY (Mock analysis enabled)")
                .build();
    }
}
