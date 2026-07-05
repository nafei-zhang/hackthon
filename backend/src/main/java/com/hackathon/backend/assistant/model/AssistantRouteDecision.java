package com.hackathon.backend.assistant.model;

public record AssistantRouteDecision(
  AssistantIntent intent,
  String standardEnglishRequest,
  String routeSource,
  String routeReasoning
) {
  public AssistantRouteDecision {
    if (intent == null) {
      throw new IllegalArgumentException("intent is required");
    }
  }

  public String standardEnglishRequestOrDefault() {
    return standardEnglishRequest == null || standardEnglishRequest.isBlank()
      ? intent.standardEnglishRequest()
      : standardEnglishRequest;
  }

  public String routeSourceOrDefault() {
    return routeSource == null || routeSource.isBlank() ? "Rule fallback" : routeSource;
  }

  public String routeReasoningOrDefault() {
    return routeReasoning == null || routeReasoning.isBlank()
      ? "Matched by the local semantic routing rules."
      : routeReasoning;
  }
}
