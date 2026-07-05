package com.hackthon.backend.assistant.service;

import com.hackthon.backend.assistant.model.AssistantRouteDecision;
import java.util.Optional;

public interface AssistantDecisionClient {
  Optional<AssistantRouteDecision> decide(String prompt, String activeTab);
}
