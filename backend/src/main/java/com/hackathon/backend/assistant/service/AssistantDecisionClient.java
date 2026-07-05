package com.hackathon.backend.assistant.service;

import com.hackathon.backend.assistant.model.AssistantRouteDecision;
import java.util.Optional;

public interface AssistantDecisionClient {
  Optional<AssistantRouteDecision> decide(String prompt, String activeTab);
}
