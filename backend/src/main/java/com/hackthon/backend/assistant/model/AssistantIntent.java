package com.hackthon.backend.assistant.model;

import java.util.Locale;

public enum AssistantIntent {
  KYC_PROFILE(
    "KYC Profile Request Recognized",
    "KYC Profile",
    "Please show me the detailed customer information for the current case.",
    "KYC profile mock data"
  ),
  PREVIOUS_INVESTIGATION(
    "Previous Case Request Recognized",
    "Previous Investigation",
    "Please show me the historical case information for the customer in the current case.",
    "Previous investigation mock data"
  ),
  TRANSACTION_REVIEW(
    "Transaction History Request Recognized",
    "Transaction Review",
    "Please show me the historical transfer records for the customer in the current case.",
    "Transaction review mock data"
  ),
  BAD_CONNECTIONS(
    "Bad Connections Request Recognized",
    "Bad Connections",
    "Please show me the bad connections information for the current case.",
    "Bad connections mock data"
  ),
  RISK_ASSESSMENT(
    "Risk Assessment Request Recognized",
    "Risk Assessment",
    "Please assess the risk level of the current case based on all related information.",
    "Aggregated risk assessment"
  ),
  CURRENT_TAB_PREVIEW(
    "Current Tab Request Recognized",
    "Current Tab Preview",
    "Please show me the records for the current workspace tab.",
    "Current tab preview data"
  );

  private final String responseTitle;
  private final String moduleLabel;
  private final String standardEnglishRequest;
  private final String responseTableTitle;

  AssistantIntent(String responseTitle, String moduleLabel, String standardEnglishRequest, String responseTableTitle) {
    this.responseTitle = responseTitle;
    this.moduleLabel = moduleLabel;
    this.standardEnglishRequest = standardEnglishRequest;
    this.responseTableTitle = responseTableTitle;
  }

  public String responseTitle() {
    return responseTitle;
  }

  public String moduleLabel() {
    return moduleLabel;
  }

  public String standardEnglishRequest() {
    return standardEnglishRequest;
  }

  public String responseTableTitle() {
    return responseTableTitle;
  }

  public static AssistantIntent fromActiveTab(String activeTab) {
    return switch (activeTab) {
      case "previous-investigation" -> PREVIOUS_INVESTIGATION;
      case "transaction-review" -> TRANSACTION_REVIEW;
      case "bad-connections" -> BAD_CONNECTIONS;
      case "kyc-profile" -> KYC_PROFILE;
      default -> KYC_PROFILE;
    };
  }

  public static AssistantIntent fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.trim()
      .toUpperCase(Locale.ROOT)
      .replace('-', '_')
      .replace(' ', '_');

    return switch (normalized) {
      case "KYC", "KYC_PROFILE" -> KYC_PROFILE;
      case "PREVIOUS", "PREVIOUS_CASE", "PREVIOUS_INVESTIGATION" -> PREVIOUS_INVESTIGATION;
      case "TRANSACTION", "TRANSACTION_REVIEW", "TRANSACTION_HISTORY" -> TRANSACTION_REVIEW;
      case "BAD_CONNECTION", "BAD_CONNECTIONS" -> BAD_CONNECTIONS;
      case "RISK", "RISK_ASSESSMENT" -> RISK_ASSESSMENT;
      case "CURRENT_TAB", "CURRENT_TAB_PREVIEW" -> CURRENT_TAB_PREVIEW;
      default -> null;
    };
  }
}
