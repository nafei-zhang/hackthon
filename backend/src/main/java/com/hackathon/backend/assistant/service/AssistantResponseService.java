package com.hackathon.backend.assistant.service;

import com.hackathon.backend.assistant.model.AssistantIntent;
import com.hackathon.backend.assistant.model.AssistantRouteDecision;
import com.hackathon.backend.casefile.model.CaseSummary;
import com.hackathon.backend.casefile.model.TableQuery;
import com.hackathon.backend.casefile.service.CaseSummaryService;
import com.hackathon.backend.casefile.service.MockCaseDataService;
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
public class AssistantResponseService {

  private static final String DEFAULT_CASE_ID = "FC260305617670";
  private static final int DATA_PAGE_SIZE = 50;
  private static final int PREVIEW_LIMIT = 5;
  private static final Pattern STREAM_CHUNK_PATTERN = Pattern.compile("\\S+|\\s+");

  private final CaseSummaryService caseSummaryService;
  private final MockCaseDataService mockCaseDataService;
  private final AssistantRoutingService assistantRoutingService;

  public AssistantResponseService(
    CaseSummaryService caseSummaryService,
    MockCaseDataService mockCaseDataService,
    AssistantRoutingService assistantRoutingService
  ) {
    this.caseSummaryService = caseSummaryService;
    this.mockCaseDataService = mockCaseDataService;
    this.assistantRoutingService = assistantRoutingService;
  }

  public String buildHtml(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = resolveCaseId(caseId);
    AssistantRouteDecision decision = assistantRoutingService.resolve(prompt, activeTab);
    AssistantIntent intent = decision.intent();

    return switch (intent) {
      case KYC_PROFILE -> buildDatasetHtml(prompt, resolvedCaseId, decision, getKycRows(resolvedCaseId));
      case PREVIOUS_INVESTIGATION -> buildDatasetHtml(prompt, resolvedCaseId, decision, getPreviousInvestigationRows(resolvedCaseId));
      case TRANSACTION_REVIEW -> buildDatasetHtml(prompt, resolvedCaseId, decision, getTransactionReviewRows(resolvedCaseId));
      case BAD_CONNECTIONS -> buildDatasetHtml(prompt, resolvedCaseId, decision, getBadConnectionsRows(resolvedCaseId));
      case RISK_ASSESSMENT -> buildRiskAssessmentHtml(prompt, resolvedCaseId, decision);
      case CURRENT_TAB_PREVIEW -> buildDatasetHtml(
        prompt,
        resolvedCaseId,
        new AssistantRouteDecision(
          AssistantIntent.fromActiveTab(activeTab),
          decision.standardEnglishRequestOrDefault(),
          decision.routeSourceOrDefault(),
          decision.routeReasoningOrDefault()
        ),
        getRowsForTab(resolvedCaseId, activeTab)
      );
    };
  }

  public List<String> buildStreamChunks(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = resolveCaseId(caseId);
    AssistantRouteDecision decision = assistantRoutingService.resolve(prompt, activeTab);
    AssistantIntent intent = decision.intent();

    String markdown = switch (intent) {
      case KYC_PROFILE -> buildDatasetMarkdown(prompt, resolvedCaseId, decision, getKycRows(resolvedCaseId));
      case PREVIOUS_INVESTIGATION -> buildDatasetMarkdown(prompt, resolvedCaseId, decision, getPreviousInvestigationRows(resolvedCaseId));
      case TRANSACTION_REVIEW -> buildDatasetMarkdown(prompt, resolvedCaseId, decision, getTransactionReviewRows(resolvedCaseId));
      case BAD_CONNECTIONS -> buildDatasetMarkdown(prompt, resolvedCaseId, decision, getBadConnectionsRows(resolvedCaseId));
      case RISK_ASSESSMENT -> buildRiskAssessmentMarkdown(prompt, resolvedCaseId, decision);
      case CURRENT_TAB_PREVIEW -> buildDatasetMarkdown(
        prompt,
        resolvedCaseId,
        new AssistantRouteDecision(
          AssistantIntent.fromActiveTab(activeTab),
          decision.standardEnglishRequestOrDefault(),
          decision.routeSourceOrDefault(),
          decision.routeReasoningOrDefault()
        ),
        getRowsForTab(resolvedCaseId, activeTab)
      );
    };

    return STREAM_CHUNK_PATTERN.matcher(markdown)
      .results()
      .map(MatchResult::group)
      .toList();
  }

  private String buildDatasetHtml(String prompt, String caseId, AssistantRouteDecision decision, List<Map<String, Object>> rows) {
    AssistantIntent intent = decision.intent();
    List<Map<String, Object>> previewRows = previewRows(rows);

    return """
      <section style="display: grid; gap: 14px; color: #0f172a;">
        <div style="padding: 16px 18px; border: 1px solid #dbe7f5; border-radius: 14px; background: linear-gradient(180deg, #f8fbff 0%%, #f2f7ff 100%%);">
          <div style="display: inline-flex; align-items: center; gap: 8px; margin-bottom: 10px; padding: 5px 10px; border-radius: 999px; background: #dbeafe; color: #1d4ed8; font-size: 12px; font-weight: 600;">
            Natural language recognized
          </div>
          <h3 style="margin: 0 0 8px; font-size: 16px; line-height: 1.4; color: #0f172a;">%s</h3>
          <p style="margin: 0 0 10px; font-size: 13px; line-height: 1.7; color: #334155;"><strong style="color: #0f172a;">Original request:</strong> %s</p>
          <p style="margin: 0; font-size: 13px; line-height: 1.7; color: #334155;"><strong style="color: #0f172a;">Standard English request:</strong> %s</p>
        </div>
        <div style="display: flex; flex-wrap: wrap; gap: 8px;">
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Matched module:</strong> %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Case:</strong> %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Rows returned:</strong> %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Route source:</strong> %s</span>
        </div>
        <div style="padding: 14px 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Semantic recognition outcome</div>
          <ul style="margin: 0; padding-left: 18px; color: #475569; font-size: 13px; line-height: 1.8;">
            <li>The request was matched to the <strong>%s</strong> workflow.</li>
            <li>%s</li>
            <li>The assistant returned the preset dataset for the current case.</li>
            <li>The table below shows the first %s records for quick review.</li>
          </ul>
        </div>
        <div style="border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff; overflow: hidden;">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #eef2f7; background: #f8fafc;">
            <div>
              <div style="font-size: 13px; font-weight: 600; color: #0f172a;">%s</div>
              <div style="margin-top: 2px; font-size: 12px; color: #64748b;">Preset data returned after semantic intent recognition.</div>
            </div>
            <div style="padding: 4px 10px; border-radius: 999px; background: #eff6ff; color: #2563eb; font-size: 12px; font-weight: 600;">Preview</div>
          </div>
          <div style="overflow-x: auto; padding: 8px;">
            %s
          </div>
        </div>
      </section>
      """.formatted(
      escapeHtml(intent.responseTitle()),
      escapeHtml(prompt),
      escapeHtml(decision.standardEnglishRequestOrDefault()),
      escapeHtml(intent.moduleLabel()),
      escapeHtml(caseId),
      String.valueOf(rows.size()),
      escapeHtml(decision.routeSourceOrDefault()),
      escapeHtml(intent.moduleLabel()),
      escapeHtml(decision.routeReasoningOrDefault()),
      String.valueOf(previewRows.size()),
      escapeHtml(intent.responseTableTitle()),
      buildTableHtml(previewRows)
    );
  }

  private String buildDatasetMarkdown(String prompt, String caseId, AssistantRouteDecision decision, List<Map<String, Object>> rows) {
    AssistantIntent intent = decision.intent();
    List<Map<String, Object>> previewRows = previewRows(rows);

    return """
      ## %s

      **Original request:** %s  
      **Standard English request:** %s  
      **Matched module:** %s  
      **Case:** %s  
      **Rows returned:** %s
      **Route source:** %s

      ### Semantic recognition outcome

      - The request was matched to the **%s** workflow.
      - %s
      - The assistant returned the preset dataset for the current case.
      - The table below shows the first %s records for quick review.

      ### %s

      %s
      """.formatted(
      escapeMarkdown(intent.responseTitle()),
      escapeMarkdown(prompt),
      escapeMarkdown(decision.standardEnglishRequestOrDefault()),
      escapeMarkdown(intent.moduleLabel()),
      escapeMarkdown(caseId),
      String.valueOf(rows.size()),
      escapeMarkdown(decision.routeSourceOrDefault()),
      escapeMarkdown(intent.moduleLabel()),
      escapeMarkdown(decision.routeReasoningOrDefault()),
      String.valueOf(previewRows.size()),
      escapeMarkdown(intent.responseTableTitle()),
      buildMarkdownTable(previewRows)
    );
  }

  private String buildRiskAssessmentHtml(String prompt, String caseId, AssistantRouteDecision decision) {
    CaseSummary summary = caseSummaryService.buildCaseSummary(caseId);
    List<Map<String, Object>> kycRows = getKycRows(caseId);
    List<Map<String, Object>> previousRows = getPreviousInvestigationRows(caseId);
    List<Map<String, Object>> transactionRows = getTransactionReviewRows(caseId);
    List<Map<String, Object>> badConnectionRows = getBadConnectionsRows(caseId);
    RiskAssessment assessment = assessRisk(caseId, summary, kycRows, previousRows, transactionRows, badConnectionRows);

    String signalTable = buildTableHtml(List.of(
      row("signal", "KYC profile records", "value", kycRows.size()),
      row("signal", "Previous investigations", "value", previousRows.size()),
      row("signal", "Escalated transactions", "value", assessment.escalatedTransactions()),
      row("signal", "High-risk bad connections", "value", assessment.highRiskConnections()),
      row("signal", "Risk score", "value", assessment.score())
    ));

    return """
      <section style="display: grid; gap: 14px; color: #0f172a;">
        <div style="padding: 16px 18px; border: 1px solid #dbe7f5; border-radius: 14px; background: linear-gradient(180deg, #f8fbff 0%%, #f2f7ff 100%%);">
          <div style="display: inline-flex; align-items: center; gap: 8px; margin-bottom: 10px; padding: 5px 10px; border-radius: 999px; background: #dbeafe; color: #1d4ed8; font-size: 12px; font-weight: 600;">
            LLM-style risk assessment
          </div>
          <h3 style="margin: 0 0 8px; font-size: 16px; line-height: 1.4; color: #0f172a;">%s</h3>
          <p style="margin: 0 0 10px; font-size: 13px; line-height: 1.7; color: #334155;"><strong style="color: #0f172a;">Original request:</strong> %s</p>
          <p style="margin: 0; font-size: 13px; line-height: 1.7; color: #334155;"><strong style="color: #0f172a;">Standard English request:</strong> %s</p>
        </div>
        <div style="display: flex; flex-wrap: wrap; gap: 8px;">
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Case:</strong> %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: %s; color: %s; font-size: 12px; font-weight: 600;">Risk level: %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Owner:</strong> %s</span>
          <span style="display: inline-flex; align-items: center; padding: 5px 10px; border-radius: 999px; background: #ffffff; border: 1px solid #dbe7f5; font-size: 12px; color: #334155;"><strong style="margin-right: 4px; color: #0f172a;">Route source:</strong> %s</span>
        </div>
        <div style="padding: 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Risk assessment conclusion</div>
          <p style="margin: 0 0 10px; font-size: 13px; line-height: 1.8; color: #334155;">%s</p>
          <p style="margin: 0 0 10px; font-size: 12px; line-height: 1.7; color: #64748b;"><strong style="color: #475467;">Routing note:</strong> %s</p>
          <ul style="margin: 0; padding-left: 18px; color: #475569; font-size: 13px; line-height: 1.8;">%s</ul>
        </div>
        <div style="border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff; overflow: hidden;">
          <div style="padding: 12px 16px; border-bottom: 1px solid #eef2f7; background: #f8fafc;">
            <div style="font-size: 13px; font-weight: 600; color: #0f172a;">Aggregated risk signals</div>
            <div style="margin-top: 2px; font-size: 12px; color: #64748b;">The backend aggregated KYC profile, previous investigation, transaction review, and bad connections data.</div>
          </div>
          <div style="overflow-x: auto; padding: 8px;">
            %s
          </div>
        </div>
        <div style="padding: 14px 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Reasoning prompt template</div>
          <pre style="margin: 0; white-space: pre-wrap; font-size: 12px; line-height: 1.7; color: #334155; background: #f8fafc; border: 1px solid #eef2f7; border-radius: 12px; padding: 12px;">%s</pre>
        </div>
      </section>
      """.formatted(
      escapeHtml(AssistantIntent.RISK_ASSESSMENT.responseTitle()),
      escapeHtml(prompt),
      escapeHtml(decision.standardEnglishRequestOrDefault()),
      escapeHtml(caseId),
      assessment.levelBackground(),
      assessment.levelColor(),
      escapeHtml(assessment.level()),
      escapeHtml(summary.owner()),
      escapeHtml(decision.routeSourceOrDefault()),
      escapeHtml(assessment.conclusion()),
      escapeHtml(decision.routeReasoningOrDefault()),
      assessment.evidence().stream().map(item -> "<li>" + escapeHtml(item) + "</li>").collect(Collectors.joining()),
      signalTable,
      escapeHtml(assessment.reasoningPrompt())
    );
  }

  private String buildRiskAssessmentMarkdown(String prompt, String caseId, AssistantRouteDecision decision) {
    CaseSummary summary = caseSummaryService.buildCaseSummary(caseId);
    List<Map<String, Object>> kycRows = getKycRows(caseId);
    List<Map<String, Object>> previousRows = getPreviousInvestigationRows(caseId);
    List<Map<String, Object>> transactionRows = getTransactionReviewRows(caseId);
    List<Map<String, Object>> badConnectionRows = getBadConnectionsRows(caseId);
    RiskAssessment assessment = assessRisk(caseId, summary, kycRows, previousRows, transactionRows, badConnectionRows);

    String signalTable = buildMarkdownTable(List.of(
      row("signal", "KYC profile records", "value", kycRows.size()),
      row("signal", "Previous investigations", "value", previousRows.size()),
      row("signal", "Escalated transactions", "value", assessment.escalatedTransactions()),
      row("signal", "High-risk bad connections", "value", assessment.highRiskConnections()),
      row("signal", "Risk score", "value", assessment.score())
    ));

    return """
      ## %s

      **Original request:** %s  
      **Standard English request:** %s  
      **Case:** %s  
      **Owner:** %s  
      **Risk level:** %s
      **Route source:** %s

      ### Risk assessment conclusion

      %s

      _Routing note: %s_

      ### Key evidence

      %s

      ### Aggregated risk signals

      %s

      ### Reasoning prompt template

      ```
      %s
      ```
      """.formatted(
      escapeMarkdown(AssistantIntent.RISK_ASSESSMENT.responseTitle()),
      escapeMarkdown(prompt),
      escapeMarkdown(decision.standardEnglishRequestOrDefault()),
      escapeMarkdown(caseId),
      escapeMarkdown(summary.owner()),
      escapeMarkdown(assessment.level()),
      escapeMarkdown(decision.routeSourceOrDefault()),
      escapeMarkdown(assessment.conclusion()),
      escapeMarkdown(decision.routeReasoningOrDefault()),
      assessment.evidence().stream().map(item -> "- " + escapeMarkdown(item)).collect(Collectors.joining("\n")),
      signalTable,
      escapeMarkdown(assessment.reasoningPrompt())
    );
  }

  private RiskAssessment assessRisk(
    String caseId,
    CaseSummary summary,
    List<Map<String, Object>> kycRows,
    List<Map<String, Object>> previousRows,
    List<Map<String, Object>> transactionRows,
    List<Map<String, Object>> badConnectionRows
  ) {
    long rmManagedGaps = kycRows.stream()
      .filter(row -> Objects.equals(row.get("rmManaged"), "Not RM managed"))
      .count();
    long priorHighCases = previousRows.stream()
      .filter(row -> Objects.equals(row.get("riskCategory"), "High"))
      .count();
    long priorEscalations = previousRows.stream()
      .filter(row -> String.valueOf(row.get("conclusion")).contains("Escalated"))
      .count();
    long escalatedTransactions = transactionRows.stream()
      .filter(row -> Objects.equals(row.get("reviewStatus"), "Escalated"))
      .count();
    long highRiskConnections = badConnectionRows.stream()
      .filter(row -> Objects.equals(row.get("riskLevel"), "High"))
      .count();
    long mediumRiskConnections = badConnectionRows.stream()
      .filter(row -> Objects.equals(row.get("riskLevel"), "Medium"))
      .count();

    int seedModifier = Math.floorMod(caseId.chars().sum(), 9) - 4;
    int score = (int) (highRiskConnections * 2 + escalatedTransactions + priorHighCases + priorEscalations + (rmManagedGaps / 4) + seedModifier);

    String level;
    String levelBackground;
    String levelColor;
    if (score >= 30) {
      level = "High";
      levelBackground = "#fef2f2";
      levelColor = "#b42318";
    } else if (score >= 18) {
      level = "Medium";
      levelBackground = "#fff7ed";
      levelColor = "#c2410c";
    } else {
      level = "Low";
      levelBackground = "#eff6ff";
      levelColor = "#1d4ed8";
    }

    List<String> evidence = List.of(
      "%d historical investigations are tagged as High risk.".formatted(priorHighCases),
      "%d historical investigations were previously escalated.".formatted(priorEscalations),
      "%d transaction review records are currently marked as Escalated.".formatted(escalatedTransactions),
      "%d bad connections are currently rated High risk, with %d more at Medium risk.".formatted(highRiskConnections, mediumRiskConnections),
      "%d KYC profile records are not RM managed and should be checked for ownership gaps.".formatted(rmManagedGaps)
    );

    String conclusion = switch (level) {
      case "High" -> "The current case should be treated as High risk because multiple prior investigations, escalated transactions, and risky connection overlaps are present at the same time.";
      case "Medium" -> "The current case should be treated as Medium risk. Several risk indicators are present, but the combined signal does not yet justify an immediate critical escalation.";
      default -> "The current case can be treated as Low risk for now. The aggregated indicators remain limited and can be monitored through normal review.";
    };

    String reasoningPrompt = """
      You are an investigator working inside Investigator Workspace.
      Assess the risk level of the current case using the aggregated case context below.
      Return: 1) risk level (Low/Medium/High), 2) short conclusion, 3) key evidence bullets, 4) recommended next actions.

      Case summary:
      - Case ID: %s
      - Owner: %s
      - Status: %s
      - Updated At: %s

      Aggregated signals:
      - KYC profile rows: %s
      - Previous investigations: %s
      - Prior High-risk investigations: %s
      - Prior escalations: %s
      - Transaction review rows: %s
      - Escalated transactions: %s
      - Bad connections rows: %s
      - High-risk bad connections: %s
      - Medium-risk bad connections: %s
      - Not RM managed rows: %s
      """.formatted(
      caseId,
      summary.owner(),
      summary.status(),
      summary.updatedAt(),
      kycRows.size(),
      previousRows.size(),
      priorHighCases,
      priorEscalations,
      transactionRows.size(),
      escalatedTransactions,
      badConnectionRows.size(),
      highRiskConnections,
      mediumRiskConnections,
      rmManagedGaps
    );

    return new RiskAssessment(
      level,
      levelBackground,
      levelColor,
      score,
      conclusion,
      evidence,
      reasoningPrompt,
      (int) escalatedTransactions,
      (int) highRiskConnections
    );
  }

  private String resolveCaseId(String caseId) {
    return caseId == null || caseId.isBlank() ? DEFAULT_CASE_ID : caseId;
  }

  private List<Map<String, Object>> getRowsForTab(String caseId, String activeTab) {
    return switch (activeTab) {
      case "previous-investigation" -> getPreviousInvestigationRows(caseId);
      case "transaction-review" -> getTransactionReviewRows(caseId);
      case "bad-connections" -> getBadConnectionsRows(caseId);
      case "kyc-profile" -> getKycRows(caseId);
      default -> getKycRows(caseId);
    };
  }

  private List<Map<String, Object>> getKycRows(String caseId) {
    return mockCaseDataService.buildKycProfileResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getPreviousInvestigationRows(String caseId) {
    return mockCaseDataService.buildPreviousInvestigationResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getTransactionReviewRows(String caseId) {
    return mockCaseDataService.buildTransactionReviewResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getBadConnectionsRows(String caseId) {
    return mockCaseDataService.buildBadConnectionsResponse(caseId, assistantQuery()).data();
  }

  private TableQuery assistantQuery() {
    return new TableQuery(1, DATA_PAGE_SIZE, "", "", null, null, Map.of());
  }

  private List<Map<String, Object>> previewRows(List<Map<String, Object>> rows) {
    return rows.stream().limit(PREVIEW_LIMIT).toList();
  }

  private String buildTableHtml(List<Map<String, Object>> rows) {
    if (rows.isEmpty()) {
      return "<p style=\"margin: 12px; color: #6b7280;\">No data available.</p>";
    }

    List<String> columns = rows.get(0).keySet().stream().limit(6).toList();

    String tableHead = columns.stream()
      .map(column -> "<th style=\"padding: 11px 12px; text-align: left; font-size: 12px; font-weight: 600; letter-spacing: 0.01em; color: #475467; background: #f8fafc; border-bottom: 1px solid #e5e7eb; white-space: nowrap;\">"
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
          + formatTableCell(column, row.getOrDefault(column, ""))
          + "</td>")
        .collect(Collectors.joining("", "<tr>", "</tr>"));
    }

    return "<table style=\"width: 100%; border-collapse: separate; border-spacing: 0; min-width: 620px; overflow: hidden;\">"
      + "<thead><tr>" + tableHead + "</tr></thead>"
      + "<tbody>" + tableBody + "</tbody>"
      + "</table>";
  }

  private String buildMarkdownTable(List<Map<String, Object>> rows) {
    if (rows.isEmpty()) {
      return "No data available.";
    }

    List<String> columns = rows.get(0).keySet().stream().limit(6).toList();
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

  private String formatTableCell(String column, Object value) {
    String rawValue = String.valueOf(value);
    String escapedValue = escapeHtml(rawValue);
    String normalizedColumn = column.toLowerCase(Locale.ROOT);

    if (normalizedColumn.contains("status")
      || normalizedColumn.contains("risk")
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

    if (column.equals("amount") || column.equals("salary") || column.equals("value")) {
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

  private String escapeHtml(String value) {
    return value
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;");
  }

  private String escapeMarkdown(String value) {
    return value
      .replace("\\", "\\\\")
      .replace("*", "\\*")
      .replace("_", "\\_")
      .replace("`", "\\`");
  }

  private String escapeMarkdownTableCell(String value) {
    return escapeMarkdown(value).replace("|", "\\|").replace("\n", " ");
  }

  private Map<String, Object> row(Object... values) {
    Map<String, Object> row = new LinkedHashMap<>();

    for (int index = 0; index < values.length; index += 2) {
      row.put(String.valueOf(values[index]), values[index + 1]);
    }

    return row;
  }

  private record RiskAssessment(
    String level,
    String levelBackground,
    String levelColor,
    int score,
    String conclusion,
    List<String> evidence,
    String reasoningPrompt,
    int escalatedTransactions,
    int highRiskConnections
  ) {
  }
}
