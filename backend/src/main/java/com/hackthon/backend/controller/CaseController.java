package com.hackthon.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackthon.backend.model.CaseSummary;
import com.hackthon.backend.model.PagedResponse;
import com.hackthon.backend.model.TableQuery;
import com.hackthon.backend.service.MockDataService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

  private final MockDataService mockDataService;
  private final ObjectMapper objectMapper;

  public CaseController(MockDataService mockDataService, ObjectMapper objectMapper) {
    this.mockDataService = mockDataService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/{caseId}/summary")
  public CaseSummary getCaseSummary(@PathVariable String caseId) {
    return mockDataService.buildCaseSummary(caseId);
  }

  @GetMapping("/{caseId}/kyc-profile")
  public PagedResponse<Map<String, Object>> getKycProfile(
    @PathVariable String caseId,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String globalSearch,
    @RequestParam(required = false) String sortField,
    @RequestParam(required = false) String sortOrder,
    @RequestParam(required = false) String filters
  ) {
    return mockDataService.buildKycProfileResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
  }

  @GetMapping("/{caseId}/previous-investigation")
  public PagedResponse<Map<String, Object>> getPreviousInvestigation(
    @PathVariable String caseId,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String globalSearch,
    @RequestParam(required = false) String sortField,
    @RequestParam(required = false) String sortOrder,
    @RequestParam(required = false) String filters
  ) {
    return mockDataService.buildPreviousInvestigationResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
  }

  @GetMapping("/{caseId}/transaction-review")
  public PagedResponse<Map<String, Object>> getTransactionReview(
    @PathVariable String caseId,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String globalSearch,
    @RequestParam(required = false) String sortField,
    @RequestParam(required = false) String sortOrder,
    @RequestParam(required = false) String filters
  ) {
    return mockDataService.buildTransactionReviewResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
  }

  @GetMapping("/{caseId}/bad-connections")
  public PagedResponse<Map<String, Object>> getBadConnections(
    @PathVariable String caseId,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String globalSearch,
    @RequestParam(required = false) String sortField,
    @RequestParam(required = false) String sortOrder,
    @RequestParam(required = false) String filters
  ) {
    return mockDataService.buildBadConnectionsResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
  }

  private TableQuery buildQuery(
    Integer page,
    Integer pageSize,
    String keyword,
    String globalSearch,
    String sortField,
    String sortOrder,
    String filters
  ) {
    return new TableQuery(
      page == null ? 1 : page,
      pageSize == null ? 10 : pageSize,
      keyword == null ? "" : keyword,
      globalSearch == null ? "" : globalSearch,
      sortField,
      sortOrder,
      parseFilters(filters)
    );
  }

  private Map<String, List<String>> parseFilters(String filters) {
    if (filters == null || filters.isBlank()) {
      return Map.of();
    }

    try {
      return objectMapper.readValue(filters, new TypeReference<>() {
      });
    } catch (Exception exception) {
      throw new IllegalArgumentException("Invalid filters query parameter", exception);
    }
  }
}
