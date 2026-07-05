package com.hackathon.backend.assistant.service;

import com.hackathon.backend.assistant.model.AssistantIntent;
import com.hackathon.backend.assistant.model.AssistantRouteDecision;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AssistantRoutingService {

  private final AssistantDecisionClient assistantDecisionClient;

  public AssistantRoutingService(AssistantDecisionClient assistantDecisionClient) {
    this.assistantDecisionClient = assistantDecisionClient;
  }

  public AssistantRouteDecision resolve(String prompt, String activeTab) {
    Optional<AssistantRouteDecision> aiDecision = assistantDecisionClient.decide(prompt, activeTab)
      .map(this::normalizeDecision);

    if (aiDecision.isPresent()) {
      return aiDecision.get();
    }

    AssistantIntent fallbackIntent = detectIntent(prompt, activeTab);
    return new AssistantRouteDecision(
      fallbackIntent,
      fallbackIntent.standardEnglishRequest(),
      "Rule fallback",
      "Matched by the local semantic routing rules."
    );
  }

  private AssistantRouteDecision normalizeDecision(AssistantRouteDecision decision) {
    return new AssistantRouteDecision(
      decision.intent(),
      decision.standardEnglishRequestOrDefault(),
      decision.routeSourceOrDefault(),
      decision.routeReasoningOrDefault()
    );
  }

  private AssistantIntent detectIntent(String prompt, String activeTab) {
    String normalized = normalizePrompt(prompt);
    String compact = normalized.replace(" ", "");

    if (isRiskAssessmentIntent(normalized, compact)) {
      return AssistantIntent.RISK_ASSESSMENT;
    }

    if (isKycIntent(normalized, compact)) {
      return AssistantIntent.KYC_PROFILE;
    }

    if (isPreviousInvestigationIntent(normalized, compact)) {
      return AssistantIntent.PREVIOUS_INVESTIGATION;
    }

    if (isTransactionIntent(normalized, compact)) {
      return AssistantIntent.TRANSACTION_REVIEW;
    }

    if (isBadConnectionsIntent(normalized, compact)) {
      return AssistantIntent.BAD_CONNECTIONS;
    }

    return AssistantIntent.fromActiveTab(activeTab);
  }

  private boolean isKycIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前的case的用户详细信息", "当前case用户详细信息", "当前案件用户详细信息", "当前case客户详情", "当前用户信息", "当前客户信息")
      || (containsAny(normalized, "kyc", "profile", "user details", "user detail", "customer details", "customer detail", "customer information", "client profile")
      && containsAny(normalized, "current case", "this case", "for this case", "current customer", "this customer", "user in the current case"));
  }

  private boolean isPreviousInvestigationIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case所属用户的历史case信息", "当前case所属用户的历史case信息", "当前案件历史case信息", "历史案例信息", "历史调查信息")
      || (containsAny(normalized, "history", "historical", "previous", "prior", "past")
      && containsAny(normalized, "case", "cases", "investigation", "investigations", "alerts")
      && containsAny(normalized, "customer", "user", "client"));
  }

  private boolean isTransactionIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case所属用户的历史转账信息", "当前case所属用户的历史转账信息", "当前案件历史转账信息", "历史交易信息", "历史转账记录")
      || (containsAny(normalized, "history", "historical", "previous", "prior", "past")
      && containsAny(normalized, "transaction", "transactions", "transfer", "transfers", "payment", "payments", "remittance", "wire"));
  }

  private boolean isBadConnectionsIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case的badconnections信息", "当前case的badconnections信息", "badconnections信息", "关联风险人员", "关联风险信息", "坏连接信息")
      || containsAny(normalized, "bad connections", "bad connection", "risky connections", "risk connections", "linked risky people", "suspicious connections", "connection risk");
  }

  private boolean isRiskAssessmentIntent(String normalized, String compact) {
    return containsAny(compact, "帮我判断当前case的风险", "判断当前case的风险", "评估当前case的风险", "风险研判", "当前案件风险")
      || (containsAny(normalized, "risk", "risky")
      && containsAny(normalized, "assess", "assessment", "evaluate", "evaluation", "judge", "determine", "analyse", "analyze"));
  }

  private String normalizePrompt(String prompt) {
    return prompt == null
      ? ""
      : prompt.toLowerCase(Locale.ROOT).replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fff]+", " ").trim();
  }

  private boolean containsAny(String value, String... tokens) {
    for (String token : tokens) {
      if (value.contains(token.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }

    return false;
  }
}
