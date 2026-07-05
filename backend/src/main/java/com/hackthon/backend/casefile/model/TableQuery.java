package com.hackthon.backend.casefile.model;

import java.util.List;
import java.util.Map;

public record TableQuery(
  int page,
  int pageSize,
  String keyword,
  String globalSearch,
  String sortField,
  String sortOrder,
  Map<String, List<String>> filters
) {

  public static TableQuery empty() {
    return new TableQuery(1, 10, "", "", null, null, Map.of());
  }
}
