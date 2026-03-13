package com.mindlog.dto;

import java.time.LocalDateTime;

public record ResonanceSnippetResponse(String bodyText, LocalDateTime occurredAt) {}
