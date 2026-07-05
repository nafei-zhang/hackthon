package com.hackthon.backend.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "assistant.ai")
public class AssistantAiProperties {

  private boolean enabled;
  private String provider = "generic";
  private String baseUrl;
  private String apiKey;
  private String model = "gpt-4.1-mini";
  private String chatCompletionsPath = "/chat/completions";
  private String githubApiVersion = "2026-03-10";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getChatCompletionsPath() {
    return chatCompletionsPath;
  }

  public void setChatCompletionsPath(String chatCompletionsPath) {
    this.chatCompletionsPath = chatCompletionsPath;
  }

  public String getGithubApiVersion() {
    return githubApiVersion;
  }

  public void setGithubApiVersion(String githubApiVersion) {
    this.githubApiVersion = githubApiVersion;
  }

  public boolean isGithubModelsProvider() {
    return provider != null && ("github".equalsIgnoreCase(provider) || "github_models".equalsIgnoreCase(provider));
  }
}
