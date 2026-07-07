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

  public Map<String, Object> buildRiskChainData(String caseId) {
    int seed = seedFromCaseId(caseId);
    List<Map<String, Object>> nodes = new ArrayList<>();
    List<Map<String, Object>> edges = new ArrayList<>();

    List<String> riskLevels = List.of("Low", "Medium", "High", "Critical");
    List<String> customerNames = List.of("Lena Wu", "Daniel Ho", "Mira Tan", "Arjun Patel", "Sarah Chen", "Mike Liu", "Emma Wang", "Tom Zhang");
    List<String> companyNames = List.of("Orion Capital", "Blue Arc Tech", "Maritime Group", "GSNA Advisory", "Nexus Holdings", "Apex Corp");
    List<String> relationLabels = List.of("Direct relation", "Shared ownership", "Business partner", "Financial transaction", "Family relation");
    List<String> industries = List.of("Finance", "Technology", "Logistics", "Consulting", "Manufacturing");
    List<String> emailDomains = List.of("case-lab.com", "finance.net", "techmail.org", "bizmail.hk");

    nodes.add(row(
      "id", "main-customer",
      "type", "customer",
      "name", pick(customerNames, seed, 0),
      "customerId", "CUST-" + (seed + 1000),
      "riskLevel", pick(riskLevels, seed, 1),
      "isPrimary", true,
      "details", row(
        "email", "customer." + (seed + 1000) + "@" + pick(emailDomains, seed, 0),
        "mobile", buildMobile(seed + 1000),
        "occupation", pick(OCCUPATIONS, seed, 0),
        "employer", pick(EMPLOYERS, seed, 0),
        "salary", 48000 + (seed % 10) * 6500,
        "nationality", pick(NATIONALITIES, seed, 0),
        "address", (seed + 20) + " Queen's Road, " + pick(LOCATIONS, seed, 0),
        "customerSince", LocalDate.of(2014 + (seed % 8), (seed % 9) + 1, 15).toString()
      )
    ));

    int nodeCount = 5 + (seed % 3);
    for (int i = 0; i < nodeCount; i++) {
      String nodeType = i % 2 == 0 ? "customer" : "company";
      String name = nodeType.equals("customer") ? pick(customerNames, seed, i + 1) : pick(companyNames, seed, i);
      
      Map<String, Object> node = row(
        "id", "node-" + i,
        "type", nodeType,
        "name", name,
        "customerId", nodeType.equals("customer") ? "CUST-" + (seed + 1001 + i) : null,
        "riskLevel", pick(riskLevels, seed, i + 2),
        "isPrimary", false
      );

      if (nodeType.equals("customer")) {
        node.put("details", row(
          "email", "customer." + (seed + 1001 + i) + "@" + pick(emailDomains, seed, i + 1),
          "mobile", buildMobile(seed + 1001 + i),
          "occupation", pick(OCCUPATIONS, seed, i + 1),
          "employer", pick(EMPLOYERS, seed, i + 2),
          "salary", 48000 + (i + 1) * 6500,
          "nationality", pick(NATIONALITIES, seed, i + 1),
          "address", (seed + 20 + i + 1) + " Queen's Road, " + pick(LOCATIONS, seed, i + 1),
          "customerSince", LocalDate.of(2014 + ((i + 1) % 8), ((i + 1) % 9) + 1, 15).toString()
        ));
      } else {
        node.put("details", row(
          "registrationNumber", "REG-" + (seed + 2000 + i),
          "industry", pick(industries, seed, i),
          "foundedYear", 1990 + (seed % 30) + i,
          "headquarters", pick(LOCATIONS, seed, i),
          "employees", 50 + (seed % 200) + i * 20,
          "revenue", 1000000 + (seed % 5000000) + i * 500000
        ));
      }

      nodes.add(node);
    }

    for (int i = 0; i < nodeCount; i++) {
      edges.add(row(
        "id", "edge-main-" + i,
        "source", "main-customer",
        "target", "node-" + i,
        "label", pick(relationLabels, seed, i)
      ));
    }

    for (int i = 0; i < nodeCount - 1; i += 2) {
      if (i + 1 < nodeCount) {
        edges.add(row(
          "id", "edge-" + i + "-" + (i + 1),
          "source", "node-" + i,
          "target", "node-" + (i + 1),
          "label", pick(relationLabels, seed, i + nodeCount)
        ));
      }
    }

    return row(
      "nodes", nodes,
      "edges", edges
    );
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
