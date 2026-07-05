package com.hackthon.backend.model;

import jakarta.validation.constraints.NotBlank;

public record AssistantRequest(
  @NotBlank(message = "prompt is required")
  String prompt,
  String caseId,
  @NotBlank(message = "activeTab is required")
  String activeTab
) {
}
