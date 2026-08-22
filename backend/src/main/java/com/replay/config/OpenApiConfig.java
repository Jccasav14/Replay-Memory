package com.replay.config;

import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration constants and metadata documentation for REPLAY.
 * Specifies standard Bearer token security schemes, API groupings,
 * and endpoint response contracts for QA/Production environments.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";
    public static final String API_TITLE = "REPLAY: Personal Memory Engine API";
    public static final String API_VERSION = "1.0.0-qa";
    public static final String API_DESCRIPTION = """
            REST API documentation for the REPLAY Personal Memory Engine.
            Provides endpoints for user authentication, memory ingestion,
            AI analysis, people tracking, life graph exploration, and offline synchronization.
            """;
    public static final String CONTACT_NAME = "REPLAY Engineering & QA Team";
    public static final String CONTACT_EMAIL = "qa-dev@replay.app";
}
