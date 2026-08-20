package com.replay.memories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysis {

    private String summary;
    private String detailedDescription;
    private String extractedText;
    private List<String> detectedObjects;
    private List<String> detectedPeople;
    private List<String> detectedEmotions;
    private List<String> detectedCategories;
    private String modelUsed;
    private Instant processedAt;
}
