package com.replay.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Builder.Default
    private String privacyLevel = "STANDARD";

    @Builder.Default
    private boolean allowBackgroundSync = true;

    @Builder.Default
    private boolean allowAiProcessing = true;
}
