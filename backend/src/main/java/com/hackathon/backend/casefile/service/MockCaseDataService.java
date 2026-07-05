package com.hackathon.backend.casefile.service;

import com.hackathon.backend.casefile.model.PagedResponse;
import com.hackathon.backend.casefile.model.TableQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MockCaseDataService {

  private final MockCaseDataFactory mockCaseDataFactory;

  public MockCaseDataService(MockCaseDataFactory mockCaseDataFactory) {
    this.mockCaseDataFactory = mockCaseDataFactory;
  }

  public PagedResponse<Map<String, Object>> buildKycProfileResponse(String caseId, TableQuery query) {
    return buildPagedResponse(mockCaseDataFactory.buildKycProfileRows(caseId), query);
  }

  public PagedResponse<Map<String, Object>> buildPreviousInvestigationResponse(String caseId, TableQuery query) {
    return buildPagedResponse(mockCaseDataFactory.buildPreviousInvestigationRows(caseId), query);
  }

  public PagedResponse<Map<String, Object>> buildTransactionReviewResponse(String caseId, TableQuery query) {
    return buildPagedResponse(mockCaseDataFactory.buildTransactionReviewRows(caseId), query);
  }

  public PagedResponse<Map<String, Object>> buildBadConnectionsResponse(String caseId, TableQuery query) {
    return buildPagedResponse(mockCaseDataFactory.buildBadConnectionsRows(caseId), query);
  }

  public PagedResponse<Map<String, Object>> buildPagedResponse(List<Map<String, Object>> rows, TableQuery query) {
    List<Map<String, Object>> filtered = filterRows(rows, query);
    int page = Math.max(query.page(), 1);
    int pageSize = Math.max(query.pageSize(), 1);
    int start = Math.max((page - 1) * pageSize, 0);
    int end = Math.min(start + pageSize, filtered.size());
    List<Map<String, Object>> pageRows = start >= filtered.size() ? List.of() : filtered.subList(start, end);

    return new PagedResponse<>(pageRows, filtered.size(), true);
  }

  private List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, TableQuery query) {
    String keyword = normalize(query.keyword());
    String globalSearch = normalize(query.globalSearch());
    Map<String, List<String>> filters = query.filters() == null ? Map.of() : query.filters();

    List<Map<String, Object>> result = rows.stream()
      .filter(row -> matchesRow(row, keyword, globalSearch, filters))
      .collect(Collectors.toCollection(ArrayList::new));

    if (query.sortField() != null && !query.sortField().isBlank() && query.sortOrder() != null && !query.sortOrder().isBlank()) {
      result.sort((left, right) -> compareValue(left.get(query.sortField()), right.get(query.sortField()), query.sortOrder()));
    }

    return result;
  }

  private boolean matchesRow(
    Map<String, Object> row,
    String keyword,
    String globalSearch,
    Map<String, List<String>> filters
  ) {
    String values = row.values().stream()
      .filter(Objects::nonNull)
      .map(String::valueOf)
      .collect(Collectors.joining(" "))
      .toLowerCase(Locale.ROOT);

    boolean matchesKeyword = keyword.isEmpty() || values.contains(keyword);
    boolean matchesGlobal = globalSearch.isEmpty() || values.contains(globalSearch);
    boolean matchesFilters = filters.entrySet().stream().allMatch(entry -> {
      List<String> expectedValues = entry.getValue();
      if (expectedValues == null || expectedValues.isEmpty()) {
        return true;
      }

      return expectedValues.contains(String.valueOf(row.get(entry.getKey())));
    });

    return matchesKeyword && matchesGlobal && matchesFilters;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private int compareValue(Object left, Object right, String order) {
    if (Objects.equals(left, right)) {
      return 0;
    }

    if (left == null) {
      return "ascend".equals(order) ? -1 : 1;
    }

    if (right == null) {
      return "ascend".equals(order) ? 1 : -1;
    }

    int base;
    if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
      base = Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
    } else if (left instanceof Comparable leftComparable && right.getClass().isAssignableFrom(left.getClass())) {
      base = leftComparable.compareTo(right);
    } else {
      base = String.valueOf(left).compareTo(String.valueOf(right));
    }

    return "ascend".equals(order) ? base : base * -1;
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }
}
