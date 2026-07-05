package com.hackthon.backend.casefile.model;

public record CaseSummary(
  String caseId,
  String status,
  String owner,
  String updatedAt
) {
}
