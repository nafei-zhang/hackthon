package com.hackathon.backend.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.assistant.config.AssistantAiProperties;
import com.hackathon.backend.assistant.model.AssistantIntent;
import com.hackathon.backend.assistant.model.AssistantRouteDecision;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiCompatibleAssistantDecisionClient implements AssistantDecisionClient {

  private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAssistantDecisionClient.class);

  private final AssistantAiProperties properties;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public OpenAiCompatibleAssistantDecisionClient(AssistantAiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.restClient = RestClient.builder().build();
  }

  @Override
  public Optional<AssistantRouteDecision> decide(String prompt, String activeTab) {
    if (!properties.isEnabled() || isBlank(properties.getBaseUrl()) || isBlank(properties.getApiKey())) {
      return Optional.empty();
    }

    try {
      RestClient.RequestBodySpec request = restClient.post()
        .uri(resolveCompletionUrl())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
        .contentType(MediaType.APPLICATION_JSON);

      if (properties.isGithubModelsProvider()) {
        request = request.header(HttpHeaders.ACCEPT, "application/vnd.github+json");

        if (!isBlank(properties.getGithubApiVersion())) {
          request = request.header("X-GitHub-Api-Version", properties.getGithubApiVersion());
        }
      } else {
        request = request.accept(MediaType.APPLICATION_JSON);
      }

      String responseBody = request.body(buildRequestBody(prompt, activeTab))
        .retrieve()
        .body(String.class);

      return extractDecision(responseBody);
    } catch (Exception exception) {
      log.warn("Assistant AI routing call failed, falling back to local rules: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  private String resolveCompletionUrl() {
    String baseUrl = properties.getBaseUrl().endsWith("/")
      ? properties.getBaseUrl().substring(0, properties.getBaseUrl().length() - 1)
      : properties.getBaseUrl();
    String path = properties.getChatCompletionsPath().startsWith("/")
      ? properties.getChatCompletionsPath()
      : "/" + properties.getChatCompletionsPath();
    return baseUrl + path;
  }

  private Map<String, Object> buildRequestBody(String prompt, String activeTab) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", properties.getModel());
    body.put("temperature", 0);
    body.put("response_format", Map.of("type", "json_object"));
    body.put("messages", List.of(
      Map.of("role", "system", "content", systemPrompt()),
      Map.of("role", "user", "content", userPrompt(prompt, activeTab))
    ));
    return body;
  }

  private Optional<AssistantRouteDecision> extractDecision(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
      if (contentNode.isMissingNode() || contentNode.isNull() || contentNode.asText().isBlank()) {
        return Optional.empty();
      }

      String content = stripCodeFence(contentNode.asText());
      JsonNode decisionJson = objectMapper.readTree(content);
      AssistantIntent intent = AssistantIntent.fromValue(decisionJson.path("intent").asText());
      if (intent == null) {
        return Optional.empty();
      }

      return Optional.of(new AssistantRouteDecision(
        intent,
        decisionJson.path("standardEnglishRequest").asText(""),
        "AI router",
        decisionJson.path("reasoning").asText("")
      ));
    } catch (Exception exception) {
      log.warn("Assistant AI routing response parsing failed, falling back to local rules: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  private String stripCodeFence(String content) {
    String trimmed = content.trim();
    if (trimmed.startsWith("```")) {
      int firstLineBreak = trimmed.indexOf('\n');
      int closingFence = trimmed.lastIndexOf("```");
      if (firstLineBreak > -1 && closingFence > firstLineBreak) {
        return trimmed.substring(firstLineBreak + 1, closingFence).trim();
      }
    }
    return trimmed;
  }

  private String systemPrompt() {
    return """
      You are the routing layer for Investigator Workspace.
      Choose exactly one assistant intent for the user request.
      Valid intents are:
      - KYC_PROFILE
      - PREVIOUS_INVESTIGATION
      - TRANSACTION_REVIEW
      - BAD_CONNECTIONS
      - RISK_ASSESSMENT
      - CURRENT_TAB_PREVIEW

      Return strict JSON only. Do not wrap in markdown.
      Required JSON shape:
      {
        "intent": "KYC_PROFILE",
        "standardEnglishRequest": "Please show me the detailed customer information for the current case.",
        "reasoning": "Short routing reason"
      }

      Use CURRENT_TAB_PREVIEW only when the request is generic and should stay on the active workspace tab.
      """;
  }

  private String userPrompt(String prompt, String activeTab) {
    return """
      User request: %s
      Active tab: %s

      Route the request to the best matching intent and rewrite it as a concise standard English request.
      """.formatted(prompt == null ? "" : prompt, activeTab == null ? "" : activeTab);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
