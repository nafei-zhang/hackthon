package com.hackthon.backend.service;

import com.hackthon.backend.model.CaseSummary;
import com.hackthon.backend.model.PagedResponse;
import com.hackthon.backend.model.TableQuery;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MockDataService {

  private static final List<String> OWNERS = List.of("Lena Wu", "Daniel Ho", "Mira Tan", "Arjun Patel");
  private static final List<String> OCCUPATIONS = List.of("Consultant", "Trader", "Engineer", "Director");
  private static final List<String> EMPLOYERS = List.of("Orion Capital", "Blue Arc Tech", "Maritime Group", "GSNA Advisory");
  private static final List<String> NATIONALITIES = List.of("Singaporean", "Malaysian", "British", "Chinese");
  private static final List<String> LOCATIONS = List.of("Hong Kong", "Singapore", "London", "Kuala Lumpur");
  private static final List<String> RELATION_TYPES = List.of("Shared device", "Shared IP", "Repeated location", "Dormant overlap");
  private static final Pattern STREAM_CHUNK_PATTERN = Pattern.compile("\\S+|\\s+");

  public CaseSummary buildCaseSummary(String caseId) {
    int seed = seedFromCaseId(caseId);
    String updatedAt = utcDateTimeFromJs(2026, 6, (seed % 18) + 1, 10, 30);

    return new CaseSummary(caseId, "ready", pick(OWNERS, seed, 1), updatedAt);
  }

  public PagedResponse<Map<String, Object>> buildKycProfileResponse(String caseId, TableQuery query) {
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

    return buildPagedResponse(rows, query);
  }

  public PagedResponse<Map<String, Object>> buildPreviousInvestigationResponse(String caseId, TableQuery query) {
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

    return buildPagedResponse(rows, query);
  }

  public PagedResponse<Map<String, Object>> buildTransactionReviewResponse(String caseId, TableQuery query) {
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

    return buildPagedResponse(rows, query);
  }

  public PagedResponse<Map<String, Object>> buildBadConnectionsResponse(String caseId, TableQuery query) {
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

    return buildPagedResponse(rows, query);
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

  public String buildAssistantHtml(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = caseId == null || caseId.isBlank() ? "CASE-2026-0001" : caseId;
    String caseContext = caseId == null || caseId.isBlank() ? "No case ID has been set yet. Showing preview data for CASE-2026-0001." : caseId;
    List<Map<String, Object>> previewRows = buildAssistantPreviewRows(resolvedCaseId, activeTab);
    String tabLabel = formatColumnLabel(activeTab);

    return """
      <section style="display: grid; gap: 14px; color: #0f172a;">
        <div style="padding: 16px 18px; border: 1px solid #dbe7f5; border-radius: 14px; background: linear-gradient(180deg, #f8fbff 0%%, #f2f7ff 100%%);">
          <div style="display: inline-flex; align-items: center; gap: 8px; margin-bottom: 10px; padding: 5px 10px; border-radius: 999px; background: #dbeafe; color: #1d4ed8; font-size: 12px; font-weight: 600;">
            Investigator Assistant
          </div>
          <h3 style="margin: 0 0 8px; font-size: 16px; line-height: 1.4; color: #0f172a;">Case guidance summary</h3>
          <p style="margin: 0 0 10px; font-size: 13px; line-height: 1.7; color: #334155;"><strong style="color: #0f172a;">Question:</strong> %s</p>
          <div style="display: flex; flex-wrap: wrap; gap: 8px;">
            <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Case:</strong> %s</span>
            <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Tab:</strong> %s</span>
            <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Preview rows:</strong> %s</span>
          </div>
        </div>
        <div style="padding: 14px 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Recommended review actions</div>
          <ul style="margin: 0; padding-left: 18px; color: #475569; font-size: 13px; line-height: 1.8;">
            <li>Review the key entities shown in the current tab before escalating.</li>
            <li>Use the global search and column filters to narrow the working set.</li>
            <li>Confirm whether the current case requires follow-up from the assigned owner.</li>
          </ul>
        </div>
        <div style="border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff; overflow: hidden;">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #eef2f7; background: #f8fafc;">
            <div>
              <div style="font-size: 13px; font-weight: 600; color: #0f172a;">Mock table preview</div>
              <div style="margin-top: 2px; font-size: 12px; color: #64748b;">Sample records from %s</div>
            </div>
            <div style="padding: 4px 10px; border-radius: 999px; background: #eff6ff; color: #2563eb; font-size: 12px; font-weight: 600;">Preview</div>
          </div>
          <div style="overflow-x: auto; padding: 8px;">
            %s
          </div>
        </div>
        <p style="margin: 0; font-size: 12px; color: #94a3b8;">This answer is returned in HTML so the client can render formatted content directly.</p>
      </section>
      """.formatted(
      escapeHtml(prompt),
      escapeHtml(caseContext),
      escapeHtml(tabLabel),
      String.valueOf(previewRows.size()),
      escapeHtml(tabLabel),
      buildAssistantTableHtml(previewRows)
    );
  }

  public List<String> buildAssistantStreamChunks(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = caseId == null || caseId.isBlank() ? "CASE-2026-0001" : caseId;
    String caseContext = caseId == null || caseId.isBlank() ? "Not set (previewing CASE-2026-0001)" : caseId;
    String tabLabel = formatColumnLabel(activeTab);
    List<Map<String, Object>> previewRows = buildAssistantPreviewRows(resolvedCaseId, activeTab);
    String markdown = """
      ## Case guidance summary

      **Question:** %s  
      **Case:** %s  
      **Active tab:** %s  
      **Preview rows:** %s

      ### Recommended review actions

      - Validate the current table results before escalating.
      - Cross-check key identifiers shown in the active tab.
      - Capture any suspicious pattern for follow-up review.

      ### Mock table preview

      %s

      _Streaming response rendered as markdown in the client._
      """.formatted(
      escapeMarkdown(prompt),
      escapeMarkdown(caseContext),
      escapeMarkdown(tabLabel),
      String.valueOf(previewRows.size()),
      buildAssistantMarkdownTable(previewRows)
    );

    return STREAM_CHUNK_PATTERN.matcher(markdown)
      .results()
      .map(MatchResult::group)
      .toList();
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

  private int seedFromCaseId(String caseId) {
    return caseId.chars().sum();
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private <T> T pick(List<T> items, int seed, int offset) {
    return items.get(Math.floorMod(seed + offset, items.size()));
  }

  private String buildMobile(int value) {
    String suffix = String.format("%06d", value);
    return "+65 98" + suffix.substring(0, 6);
  }

  private List<Map<String, Object>> buildAssistantPreviewRows(String caseId, String activeTab) {
    TableQuery previewQuery = new TableQuery(1, 3, "", "", null, null, Map.of());

    return switch (activeTab) {
      case "previous-investigation" -> buildPreviousInvestigationResponse(caseId, previewQuery).data();
      case "transaction-review" -> buildTransactionReviewResponse(caseId, previewQuery).data();
      case "bad-connections" -> buildBadConnectionsResponse(caseId, previewQuery).data();
      case "kyc-profile" -> buildKycProfileResponse(caseId, previewQuery).data();
      default -> buildKycProfileResponse(caseId, previewQuery).data();
    };
  }

  private String buildAssistantTableHtml(List<Map<String, Object>> rows) {
    if (rows.isEmpty()) {
      return "<p style=\"margin: 12px; color: #6b7280;\">No preview data available.</p>";
    }

    List<String> columns = rows.get(0).keySet().stream().limit(5).toList();

    String tableHead = columns.stream()
      .map(column -> "<th style=\"padding: 11px 12px; text-align: left; font-size: 12px; font-weight: 600; letter-spacing: 0.01em; color: #475467; background: #f8fafc; border-bottom: 1px solid #e5e7eb; white-space: nowrap; text-transform: none;\">"
        + escapeHtml(formatColumnLabel(column))
        + "</th>")
      .collect(Collectors.joining());

    String tableBody = "";
    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex += 1) {
      Map<String, Object> row = rows.get(rowIndex);
      String rowBackground = rowIndex % 2 == 0 ? "#ffffff" : "#fbfdff";

      tableBody += columns.stream()
        .map(column -> "<td style=\"padding: 11px 12px; font-size: 12px; color: #101828; border-bottom: 1px solid #f2f4f7; white-space: nowrap; background: "
          + rowBackground
          + ";\">"
          + formatAssistantCell(column, row.getOrDefault(column, ""))
          + "</td>")
        .collect(Collectors.joining("", "<tr>", "</tr>"))
      ;
    }

    return "<table style=\"width: 100%; border-collapse: separate; border-spacing: 0; min-width: 620px; overflow: hidden;\">"
      + "<thead><tr>" + tableHead + "</tr></thead>"
      + "<tbody>" + tableBody + "</tbody>"
      + "</table>";
  }

  private String buildAssistantMarkdownTable(List<Map<String, Object>> rows) {
    if (rows.isEmpty()) {
      return "No preview data available.";
    }

    List<String> columns = rows.get(0).keySet().stream().limit(5).toList();
    String header = columns.stream()
      .map(column -> escapeMarkdownTableCell(formatColumnLabel(column)))
      .collect(Collectors.joining(" | ", "| ", " |"));

    String separator = columns.stream()
      .map(column -> "---")
      .collect(Collectors.joining(" | ", "| ", " |"));

    String body = rows.stream()
      .map(row -> columns.stream()
        .map(column -> escapeMarkdownTableCell(String.valueOf(row.getOrDefault(column, ""))))
        .collect(Collectors.joining(" | ", "| ", " |")))
      .collect(Collectors.joining("\n"));

    return header + "\n" + separator + "\n" + body;
  }

  private String formatAssistantCell(String column, Object value) {
    String rawValue = String.valueOf(value);
    String escapedValue = escapeHtml(rawValue);

    if (column.toLowerCase(Locale.ROOT).contains("status")
      || column.toLowerCase(Locale.ROOT).contains("risk")
      || column.equals("rmManaged")) {
      String background = switch (rawValue) {
        case "High", "Escalated" -> "#fef2f2";
        case "Medium", "Pending" -> "#fff7ed";
        case "Low", "Cleared", "RM managed" -> "#eff6ff";
        default -> "#f8fafc";
      };
      String color = switch (rawValue) {
        case "High", "Escalated" -> "#b42318";
        case "Medium", "Pending" -> "#c2410c";
        case "Low", "Cleared", "RM managed" -> "#1d4ed8";
        default -> "#475467";
      };

      return "<span style=\"display: inline-flex; align-items: center; padding: 4px 8px; border-radius: 999px; background: "
        + background
        + "; color: "
        + color
        + "; font-weight: 600;\">"
        + escapedValue
        + "</span>";
    }

    if (column.equals("amount") || column.equals("salary")) {
      return "<span style=\"font-variant-numeric: tabular-nums; font-weight: 600; color: #0f172a;\">"
        + escapedValue
        + "</span>";
    }

    if (column.equals("id") || column.endsWith("Id") || column.endsWith("Code")) {
      return "<span style=\"font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; color: #334155;\">"
        + escapedValue
        + "</span>";
    }

    return escapedValue;
  }

  private String formatColumnLabel(String value) {
    String normalized = value
      .replace('-', ' ')
      .replaceAll("([a-z])([A-Z])", "$1 $2")
      .trim();

    if (normalized.isEmpty()) {
      return value;
    }

    String[] parts = normalized.split("\\s+");
    List<String> formatted = new ArrayList<>();

    for (String part : parts) {
      if (part.length() <= 3 && part.equals(part.toUpperCase(Locale.ROOT))) {
        formatted.add(part);
      } else {
        formatted.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
      }
    }

    return String.join(" ", formatted);
  }

  private String escapeMarkdown(String value) {
    return value
      .replace("\\", "\\\\")
      .replace("*", "\\*")
      .replace("_", "\\_")
      .replace("`", "\\`");
  }

  private String escapeMarkdownTableCell(String value) {
    return escapeMarkdown(value)
      .replace("|", "\\|")
      .replace("\n", " ");
  }

  private String escapeHtml(String value) {
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;");
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
