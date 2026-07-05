package com.hackthon.backend.service;

import com.hackthon.backend.model.AssistantRouteDecision;
import java.util.Optional;

public interface AssistantDecisionClient {
  Optional<AssistantRouteDecision> decide(String prompt, String activeTab);
}
