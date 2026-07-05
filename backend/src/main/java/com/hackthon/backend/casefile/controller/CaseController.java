package com.hackthon.backend.casefile.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackthon.backend.casefile.model.CaseSummary;
import com.hackthon.backend.casefile.model.PagedResponse;
import com.hackthon.backend.casefile.model.TableQuery;
import com.hackthon.backend.casefile.service.CaseSummaryService;
import com.hackthon.backend.casefile.service.MockCaseDataService;
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

  private final CaseSummaryService caseSummaryService;
  private final MockCaseDataService mockCaseDataService;
  private final ObjectMapper objectMapper;

  public CaseController(CaseSummaryService caseSummaryService, MockCaseDataService mockCaseDataService, ObjectMapper objectMapper) {
    this.caseSummaryService = caseSummaryService;
    this.mockCaseDataService = mockCaseDataService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/{caseId}/summary")
  public CaseSummary getCaseSummary(@PathVariable String caseId) {
    return caseSummaryService.buildCaseSummary(caseId);
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
    return mockCaseDataService.buildKycProfileResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
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
    return mockCaseDataService.buildPreviousInvestigationResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
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
    return mockCaseDataService.buildTransactionReviewResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
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
    return mockCaseDataService.buildBadConnectionsResponse(caseId, buildQuery(page, pageSize, keyword, globalSearch, sortField, sortOrder, filters));
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
