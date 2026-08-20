package com.replay.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiVisionResponse {

    private String summary;
    private String detailedDescription;
    private List<String> detectedObjects;
    private List<String> detectedPeopleDescription;
    private String detectedContextCategory;
    private List<String> detectedEmotions;
    private String extractedTextOcr;
}
