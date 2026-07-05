package com.hackathon.backend.assistant.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hackathon.backend.assistant.model.AssistantIntent;
import com.hackathon.backend.assistant.model.AssistantRouteDecision;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AssistantRoutingServiceTest {

  @Test
  void usesAiDecisionWhenAvailable() {
    AssistantRoutingService routingService = new AssistantRoutingService((prompt, activeTab) -> Optional.of(
      new AssistantRouteDecision(
        AssistantIntent.BAD_CONNECTIONS,
        "Please show me the bad connections information for the current case.",
        "AI router",
        "The prompt explicitly asks for suspicious networked relationships."
      )
    ));

    AssistantRouteDecision decision = routingService.resolve("show risky linked people", "kyc-profile");

    assertThat(decision.intent()).isEqualTo(AssistantIntent.BAD_CONNECTIONS);
    assertThat(decision.routeSourceOrDefault()).isEqualTo("AI router");
    assertThat(decision.routeReasoningOrDefault()).contains("suspicious networked relationships");
  }

  @Test
  void fallsBackToRuleRoutingWhenAiDecisionIsUnavailable() {
    AssistantRoutingService routingService = new AssistantRoutingService((prompt, activeTab) -> Optional.empty());

    AssistantRouteDecision decision = routingService.resolve("帮我查询当前case所属用户的历史转账信息", "kyc-profile");

    assertThat(decision.intent()).isEqualTo(AssistantIntent.TRANSACTION_REVIEW);
    assertThat(decision.routeSourceOrDefault()).isEqualTo("Rule fallback");
    assertThat(decision.standardEnglishRequestOrDefault())
      .isEqualTo("Please show me the historical transfer records for the customer in the current case.");
  }
}
