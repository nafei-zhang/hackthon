package com.hackathon.backend.casefile.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockCaseDataFactory {

  private static final List<String> OWNERS = List.of("Lena Wu", "Daniel Ho", "Mira Tan", "Arjun Patel");
  private static final List<String> OCCUPATIONS = List.of("Consultant", "Trader", "Engineer", "Director");
  private static final List<String> EMPLOYERS = List.of("Orion Capital", "Blue Arc Tech", "Maritime Group", "GSNA Advisory");
  private static final List<String> NATIONALITIES = List.of("Singaporean", "Malaysian", "British", "Chinese");
  private static final List<String> LOCATIONS = List.of("Hong Kong", "Singapore", "London", "Kuala Lumpur");
  private static final List<String> RELATION_TYPES = List.of("Shared device", "Shared IP", "Repeated location", "Dormant overlap");

  public List<Map<String, Object>> buildKycProfileRows(String caseId) {
    List<Map<String, Object>> rows = new ArrayList<>();
    int seed = seedFromCaseId(caseId);

    for (int index = 0; index < 22; index += 1) {
      rows.add(row(
        "id", "%s-KYC-%d".formatted(caseId, index + 1),
        "customerId", "CUST-" + (seed + index + 1000),
        "prcId", "PRC-" + (seed + index + 2200),
        "entryPermitId", "EP-" + (seed + index + 3100),
        "cinNumber", "CIN-" + (seed + index + 4100),
        "customerSince", LocalDate.of(2014 + (index % 8), (index % 9) + 1, 15).toString(),
        "rmManaged", index % 2 == 0 ? "RM managed" : "Not RM managed",
        "address", "%d Queen's Road, %s".formatted(20 + index, pick(LOCATIONS, seed, index)),
        "email", "customer%d@case-lab.com".formatted(index + 1),
        "mobile", buildMobile(seed + index),
        "occupation", pick(OCCUPATIONS, seed, index),
        "employer", pick(EMPLOYERS, seed, index + 2),
        "salary", 48000 + index * 6500,
        "nationality", pick(NATIONALITIES, seed, index),
        "workplace", pick(List.of("My Workplace", "Corporate Desk", "Regional Hub"), seed, index),
        "gsnaExposure", pick(List.of("HSBC MY", "HSBC UK", "HSBC US", "HSBC HK"), seed, index)
      ));
    }

    return rows;
  }

  public List<Map<String, Object>> buildPreviousInvestigationRows(String caseId) {
    List<Map<String, Object>> rows = new ArrayList<>();
    int seed = seedFromCaseId(caseId);

    for (int index = 0; index < 18; index += 1) {
      rows.add(row(
        "id", "%s-PI-%d".formatted(caseId, index + 1),
        "investigationType", pick(List.of("CAT A", "CAT B", "CAT C", "EDD"), seed, index),
        "referenceCode", "UCM-" + (seed + 500 + index),
        "previousOwner", pick(OWNERS, seed, index),
        "riskCategory", pick(List.of("Low", "Medium", "High"), seed, index),
        "conclusion", pick(List.of("Closed with note", "Escalated to UCM", "Monitoring only"), seed, index + 1),
        "openedAt", utcDateTimeFromJs(2025, index % 12, 5 + index, 0, 0),
        "closedAt", utcDateTimeFromJs(2025, (index % 12) + 1, 12 + index, 0, 0),
        "note", "Prior case narrative %d for %s".formatted(index + 1, caseId)
      ));
    }

    return rows;
  }

  public List<Map<String, Object>> buildTransactionReviewRows(String caseId) {
    List<Map<String, Object>> rows = new ArrayList<>();
    int seed = seedFromCaseId(caseId);

    for (int index = 0; index < 24; index += 1) {
      rows.add(row(
        "id", "%s-TR-%d".formatted(caseId, index + 1),
        "counterparty", pick(List.of("Atlas Holdings", "North Ridge", "Silver Axis", "Morning Peak"), seed, index),
        "instrumentType", pick(List.of("Cheque", "Stock"), seed, index),
        "instrumentName", pick(List.of("AIA", "HSBC", "Tencent", "Petronas"), seed, index + 1),
        "amount", 12000 + index * 5700,
        "currency", pick(List.of("USD", "HKD", "SGD"), seed, index),
        "bookingDate", utcDateTimeFromJs(2026, index % 6, 2 + index, 0, 0),
        "reviewStatus", pick(List.of("Pending", "Escalated", "Cleared"), seed, index),
        "reviewer", pick(OWNERS, seed, index + 2),
        "comment", "Counterparty and instrument review %d".formatted(index + 1)
      ));
    }

    return rows;
  }

  public List<Map<String, Object>> buildBadConnectionsRows(String caseId) {
    List<Map<String, Object>> rows = new ArrayList<>();
    int seed = seedFromCaseId(caseId);

    for (int index = 0; index < 16; index += 1) {
      rows.add(row(
        "id", "%s-BC-%d".formatted(caseId, index + 1),
        "deviceId", "DEV-" + (seed + 800 + index),
        "ipAddress", "10.%d.%d.%d".formatted((seed + index) % 255, (seed + index * 2) % 255, 50 + index),
        "lastLoginAt", utcDateTimeFromJs(2026, 5, 1 + index, 7 + (index % 8), 20),
        "location", pick(LOCATIONS, seed, index),
        "riskLevel", pick(List.of("Low", "Medium", "High"), seed, index),
        "relationType", pick(RELATION_TYPES, seed, index),
        "comment", "Observed overlap pattern %d".formatted(index + 1)
      ));
    }

    return rows;
  }

  private int seedFromCaseId(String caseId) {
    return caseId.chars().sum();
  }

  private <T> T pick(List<T> items, int seed, int offset) {
    return items.get(Math.floorMod(seed + offset, items.size()));
  }

  private String buildMobile(int value) {
    String suffix = String.format("%06d", value);
    return "+65 98" + suffix.substring(0, 6);
  }

  private String utcDateTimeFromJs(int year, int zeroBasedMonth, int day, int hour, int minute) {
    return ZonedDateTime.of(year, 1, 1, hour, minute, 0, 0, ZoneOffset.UTC)
      .plusMonths(zeroBasedMonth)
      .plusDays(day - 1L)
      .toInstant()
      .toString();
  }

  private Map<String, Object> row(Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (int index = 0; index < values.length; index += 2) {
      row.put(String.valueOf(values[index]), values[index + 1]);
    }
    return row;
  }
}
