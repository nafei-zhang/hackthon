package com.hackthon.backend.model;

public record CaseSummary(
  String caseId,
  String status,
  String owner,
  String updatedAt
) {
}
