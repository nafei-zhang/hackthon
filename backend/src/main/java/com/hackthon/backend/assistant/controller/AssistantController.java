package com.hackthon.backend.assistant.controller;

import com.hackthon.backend.assistant.model.AssistantHtmlResponse;
import com.hackthon.backend.assistant.model.AssistantRequest;
import com.hackthon.backend.assistant.service.AssistantResponseService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
  private static final long RESPONSE_DELAY_MS = 2000L;

  private final AssistantResponseService assistantResponseService;

  public AssistantController(AssistantResponseService assistantResponseService) {
    this.assistantResponseService = assistantResponseService;
  }

  @PostMapping("/xhr")
  public AssistantHtmlResponse xhr(@Valid @RequestBody AssistantRequest request) {
    failOnErrorPrompt(request.prompt());
    awaitResponseDelay();
    return new AssistantHtmlResponse(
      assistantResponseService.buildHtml(request.prompt(), request.caseId(), request.activeTab())
    );
  }

  @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<StreamingResponseBody> stream(@Valid @RequestBody AssistantRequest request) {
    failOnErrorPrompt(request.prompt());
    List<String> chunks = assistantResponseService.buildStreamChunks(request.prompt(), request.caseId(), request.activeTab());

    StreamingResponseBody responseBody = outputStream -> {
      awaitResponseDelay();

      for (String chunk : chunks) {
        outputStream.write(chunk.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        try {
          Thread.sleep(15);
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          throw new IOException("Streaming interrupted", exception);
        }
      }
    };

    return ResponseEntity
      .ok()
      .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
      .body(responseBody);
  }

  private void failOnErrorPrompt(String prompt) {
    if (prompt != null && prompt.toLowerCase().contains("error")) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assistant request failed.");
    }
  }

  private void awaitResponseDelay() {
    try {
      Thread.sleep(RESPONSE_DELAY_MS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Assistant request interrupted.", exception);
    }
  }
}
