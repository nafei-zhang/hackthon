package com.hackthon.backend.assistant.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hackthon.backend.casefile.service.CaseSummaryService;
import com.hackthon.backend.casefile.service.MockCaseDataFactory;
import com.hackthon.backend.casefile.service.MockCaseDataService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AssistantResponseServiceTest {

  private final AssistantResponseService assistantResponseService = new AssistantResponseService(
    new CaseSummaryService(),
    new MockCaseDataService(new MockCaseDataFactory()),
    new AssistantRoutingService((prompt, activeTab) -> Optional.empty())
  );

  @Test
  void returnsKycProfileForChineseUserDetailPrompt() {
    String html = assistantResponseService.buildHtml(
      "帮我查询当前的case的用户详细信息",
      "CASE-3333-3333",
      "transaction-review"
    );

    assertThat(html).contains("KYC Profile Request Recognized");
    assertThat(html).contains("Please show me the detailed customer information for the current case.");
    assertThat(html).contains("KYC profile mock data");
    assertThat(html).contains("Customer Id");
    assertThat(html).contains("Route source:</strong> Rule fallback");
  }

  @Test
  void returnsPreviousInvestigationForSemanticEnglishPrompt() {
    String html = assistantResponseService.buildHtml(
      "Please pull the previous case history for the customer in this case.",
      "CASE-3333-3333",
      "kyc-profile"
    );

    assertThat(html).contains("Previous Case Request Recognized");
    assertThat(html).contains("Please show me the historical case information for the customer in the current case.");
    assertThat(html).contains("Previous investigation mock data");
    assertThat(html).contains("Investigation Type");
  }

  @Test
  void returnsTransactionReviewForChineseTransferPromptInStreamMode() {
    List<String> chunks = assistantResponseService.buildStreamChunks(
      "帮我查询当前case所属用户的历史转账信息",
      "CASE-3333-3333",
      "kyc-profile"
    );

    String markdown = String.join("", chunks);

    assertThat(markdown).contains("Transaction History Request Recognized");
    assertThat(markdown).contains("Please show me the historical transfer records for the customer in the current case.");
    assertThat(markdown).contains("Transaction review mock data");
    assertThat(markdown).contains("| Counterparty |");
    assertThat(markdown).contains("**Route source:** Rule fallback");
  }

  @Test
  void returnsBadConnectionsForSemanticPrompt() {
    String html = assistantResponseService.buildHtml(
      "Can you show the risky connections for the current case?",
      "CASE-3333-3333",
      "transaction-review"
    );

    assertThat(html).contains("Bad Connections Request Recognized");
    assertThat(html).contains("Please show me the bad connections information for the current case.");
    assertThat(html).contains("Bad connections mock data");
    assertThat(html).contains("Device Id");
  }

  @Test
  void returnsRiskAssessmentWithAggregatedSignals() {
    String html = assistantResponseService.buildHtml(
      "帮我判断当前case的风险",
      "CASE-3333-3333",
      "kyc-profile"
    );

    assertThat(html).contains("Risk Assessment Request Recognized");
    assertThat(html).contains("Please assess the risk level of the current case based on all related information.");
    assertThat(html).contains("Risk assessment conclusion");
    assertThat(html).contains("Aggregated risk signals");
    assertThat(html).contains("Reasoning prompt template");
    assertThat(html).contains("Route source:</strong> Rule fallback");
  }
}
