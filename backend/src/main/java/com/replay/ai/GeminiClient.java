package com.replay.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.replay.ai.dto.GeminiVisionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final String apiKey;
    private final String modelText;
    private final String modelEmbedding;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Value("${replay.ai.gemini.api-key:}") String apiKey,
            @Value("${replay.ai.gemini.model-text:gemini-1.5-flash}") String modelText,
            @Value("${replay.ai.gemini.model-embedding:text-embedding-004}") String modelEmbedding,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.modelText = modelText;
        this.modelEmbedding = modelEmbedding;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public GeminiVisionResponse analyzeImage(byte[] imageBytes, String mimeType, String optionalNotes) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key not configured. Returning mock analysis response.");
            return GeminiVisionResponse.builder()
                    .summary("Memory captured on local platform")
                    .detailedDescription("Locally stored autobiographical memory entry.")
                    .detectedObjects(List.of("Scene", "Environment"))
                    .detectedContextCategory("DAILY")
                    .build();
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", "Analyze this image for REPLAY personal memory engine. Extract structured biographical metadata as JSON. Context: " + (optionalNotes != null ? optionalNotes : "")),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType,
                                            "data", base64Image
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json",
                            "temperature", 0.2
                    )
            );

            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + modelText + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String jsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return objectMapper.readValue(jsonText, GeminiVisionResponse.class);

        } catch (Exception e) {
            log.error("Failed to analyze image with Gemini API: {}", e.getMessage());
            return GeminiVisionResponse.builder()
                    .summary("Image captured: " + (optionalNotes != null ? optionalNotes : "Personal moment"))
                    .detailedDescription("Analysis could not be completed.")
                    .detectedContextCategory("DAILY")
                    .build();
        }
    }

    public List<Double> generateEmbedding(String text) {
        if (apiKey == null || apiKey.isBlank() || text == null || text.isBlank()) {
            return generateMockEmbedding(768);
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "models/" + modelEmbedding,
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + modelEmbedding + ":embedContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode valuesNode = root.path("embedding").path("values");

            List<Double> vector = new ArrayList<>();
            for (JsonNode val : valuesNode) {
                vector.add(val.asDouble());
            }
            return vector;

        } catch (Exception e) {
            log.warn("Gemini embedding generation failed ({}). Using fallback vector.", e.getMessage());
            return generateMockEmbedding(768);
        }
    }

    private List<Double> generateMockEmbedding(int dims) {
        List<Double> mock = new ArrayList<>(dims);
        for (int i = 0; i < dims; i++) {
            mock.add(0.0);
        }
        return mock;
    }
}
