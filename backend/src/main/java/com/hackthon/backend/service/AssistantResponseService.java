package com.hackthon.backend.service;

import com.hackthon.backend.model.CaseSummary;
import com.hackthon.backend.model.TableQuery;
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

  private static final String DEFAULT_CASE_ID = "CASE-2026-0001";
  private static final int DATA_PAGE_SIZE = 50;
  private static final int PREVIEW_LIMIT = 5;
  private static final Pattern STREAM_CHUNK_PATTERN = Pattern.compile("\\S+|\\s+");

  private final MockDataService mockDataService;

  public AssistantResponseService(MockDataService mockDataService) {
    this.mockDataService = mockDataService;
  }

  public String buildHtml(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = resolveCaseId(caseId);
    AssistantIntent intent = detectIntent(prompt, activeTab);

    return switch (intent) {
      case KYC_PROFILE -> buildDatasetHtml(prompt, resolvedCaseId, intent, getKycRows(resolvedCaseId));
      case PREVIOUS_INVESTIGATION -> buildDatasetHtml(prompt, resolvedCaseId, intent, getPreviousInvestigationRows(resolvedCaseId));
      case TRANSACTION_REVIEW -> buildDatasetHtml(prompt, resolvedCaseId, intent, getTransactionReviewRows(resolvedCaseId));
      case BAD_CONNECTIONS -> buildDatasetHtml(prompt, resolvedCaseId, intent, getBadConnectionsRows(resolvedCaseId));
      case RISK_ASSESSMENT -> buildRiskAssessmentHtml(prompt, resolvedCaseId);
      case CURRENT_TAB_PREVIEW -> buildDatasetHtml(prompt, resolvedCaseId, intentForActiveTab(activeTab), getRowsForTab(resolvedCaseId, activeTab));
    };
  }

  public List<String> buildStreamChunks(String prompt, String caseId, String activeTab) {
    String resolvedCaseId = resolveCaseId(caseId);
    AssistantIntent intent = detectIntent(prompt, activeTab);

    String markdown = switch (intent) {
      case KYC_PROFILE -> buildDatasetMarkdown(prompt, resolvedCaseId, intent, getKycRows(resolvedCaseId));
      case PREVIOUS_INVESTIGATION -> buildDatasetMarkdown(prompt, resolvedCaseId, intent, getPreviousInvestigationRows(resolvedCaseId));
      case TRANSACTION_REVIEW -> buildDatasetMarkdown(prompt, resolvedCaseId, intent, getTransactionReviewRows(resolvedCaseId));
      case BAD_CONNECTIONS -> buildDatasetMarkdown(prompt, resolvedCaseId, intent, getBadConnectionsRows(resolvedCaseId));
      case RISK_ASSESSMENT -> buildRiskAssessmentMarkdown(prompt, resolvedCaseId);
      case CURRENT_TAB_PREVIEW -> buildDatasetMarkdown(prompt, resolvedCaseId, intentForActiveTab(activeTab), getRowsForTab(resolvedCaseId, activeTab));
    };

    return STREAM_CHUNK_PATTERN.matcher(markdown)
      .results()
      .map(MatchResult::group)
      .toList();
  }

  private String buildDatasetHtml(String prompt, String caseId, AssistantIntent intent, List<Map<String, Object>> rows) {
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
        </div>
        <div style="padding: 14px 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Semantic recognition outcome</div>
          <ul style="margin: 0; padding-left: 18px; color: #475569; font-size: 13px; line-height: 1.8;">
            <li>The request was matched to the <strong>%s</strong> workflow.</li>
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
      escapeHtml(intent.standardEnglishRequest()),
      escapeHtml(intent.moduleLabel()),
      escapeHtml(caseId),
      String.valueOf(rows.size()),
      escapeHtml(intent.moduleLabel()),
      String.valueOf(previewRows.size()),
      escapeHtml(intent.responseTableTitle()),
      buildTableHtml(previewRows)
    );
  }

  private String buildDatasetMarkdown(String prompt, String caseId, AssistantIntent intent, List<Map<String, Object>> rows) {
    List<Map<String, Object>> previewRows = previewRows(rows);

    return """
      ## %s

      **Original request:** %s  
      **Standard English request:** %s  
      **Matched module:** %s  
      **Case:** %s  
      **Rows returned:** %s

      ### Semantic recognition outcome

      - The request was matched to the **%s** workflow.
      - The assistant returned the preset dataset for the current case.
      - The table below shows the first %s records for quick review.

      ### %s

      %s
      """.formatted(
      escapeMarkdown(intent.responseTitle()),
      escapeMarkdown(prompt),
      escapeMarkdown(intent.standardEnglishRequest()),
      escapeMarkdown(intent.moduleLabel()),
      escapeMarkdown(caseId),
      String.valueOf(rows.size()),
      escapeMarkdown(intent.moduleLabel()),
      String.valueOf(previewRows.size()),
      escapeMarkdown(intent.responseTableTitle()),
      buildMarkdownTable(previewRows)
    );
  }

  private String buildRiskAssessmentHtml(String prompt, String caseId) {
    CaseSummary summary = mockDataService.buildCaseSummary(caseId);
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
        </div>
        <div style="padding: 16px; border: 1px solid #e5e7eb; border-radius: 14px; background: #ffffff;">
          <div style="margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #0f172a;">Risk assessment conclusion</div>
          <p style="margin: 0 0 10px; font-size: 13px; line-height: 1.8; color: #334155;">%s</p>
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
      escapeHtml(AssistantIntent.RISK_ASSESSMENT.standardEnglishRequest()),
      escapeHtml(caseId),
      assessment.levelBackground(),
      assessment.levelColor(),
      escapeHtml(assessment.level()),
      escapeHtml(summary.owner()),
      escapeHtml(assessment.conclusion()),
      assessment.evidence().stream().map(item -> "<li>" + escapeHtml(item) + "</li>").collect(Collectors.joining()),
      signalTable,
      escapeHtml(assessment.reasoningPrompt())
    );
  }

  private String buildRiskAssessmentMarkdown(String prompt, String caseId) {
    CaseSummary summary = mockDataService.buildCaseSummary(caseId);
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

      ### Risk assessment conclusion

      %s

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
      escapeMarkdown(AssistantIntent.RISK_ASSESSMENT.standardEnglishRequest()),
      escapeMarkdown(caseId),
      escapeMarkdown(summary.owner()),
      escapeMarkdown(assessment.level()),
      escapeMarkdown(assessment.conclusion()),
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

  private AssistantIntent detectIntent(String prompt, String activeTab) {
    String normalized = normalizePrompt(prompt);
    String compact = normalized.replace(" ", "");

    if (isRiskAssessmentIntent(normalized, compact)) {
      return AssistantIntent.RISK_ASSESSMENT;
    }

    if (isKycIntent(normalized, compact)) {
      return AssistantIntent.KYC_PROFILE;
    }

    if (isPreviousInvestigationIntent(normalized, compact)) {
      return AssistantIntent.PREVIOUS_INVESTIGATION;
    }

    if (isTransactionIntent(normalized, compact)) {
      return AssistantIntent.TRANSACTION_REVIEW;
    }

    if (isBadConnectionsIntent(normalized, compact)) {
      return AssistantIntent.BAD_CONNECTIONS;
    }

    return AssistantIntent.CURRENT_TAB_PREVIEW;
  }

  private boolean isKycIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前的case的用户详细信息", "当前case用户详细信息", "当前案件用户详细信息", "当前case客户详情", "当前用户信息", "当前客户信息")
      || (containsAny(normalized, "kyc", "profile", "user details", "user detail", "customer details", "customer detail", "customer information", "client profile")
      && containsAny(normalized, "current case", "this case", "for this case", "current customer", "this customer", "user in the current case"));
  }

  private boolean isPreviousInvestigationIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case所属用户的历史case信息", "当前case所属用户的历史case信息", "当前案件历史case信息", "历史案例信息", "历史调查信息")
      || (containsAny(normalized, "history", "historical", "previous", "prior", "past")
      && containsAny(normalized, "case", "cases", "investigation", "investigations", "alerts")
      && containsAny(normalized, "customer", "user", "client"));
  }

  private boolean isTransactionIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case所属用户的历史转账信息", "当前case所属用户的历史转账信息", "当前案件历史转账信息", "历史交易信息", "历史转账记录")
      || (containsAny(normalized, "history", "historical", "previous", "prior", "past")
      && containsAny(normalized, "transaction", "transactions", "transfer", "transfers", "payment", "payments", "remittance", "wire"));
  }

  private boolean isBadConnectionsIntent(String normalized, String compact) {
    return containsAny(compact, "帮我查询当前case的badconnections信息", "当前case的badconnections信息", "badconnections信息", "关联风险人员", "关联风险信息", "坏连接信息")
      || containsAny(normalized, "bad connections", "bad connection", "risky connections", "risk connections", "linked risky people", "suspicious connections", "connection risk");
  }

  private boolean isRiskAssessmentIntent(String normalized, String compact) {
    return containsAny(compact, "帮我判断当前case的风险", "判断当前case的风险", "评估当前case的风险", "风险研判", "当前案件风险")
      || (containsAny(normalized, "risk", "risky")
      && containsAny(normalized, "assess", "assessment", "evaluate", "evaluation", "judge", "determine", "analyse", "analyze"));
  }

  private String resolveCaseId(String caseId) {
    return caseId == null || caseId.isBlank() ? DEFAULT_CASE_ID : caseId;
  }

  private AssistantIntent intentForActiveTab(String activeTab) {
    return switch (activeTab) {
      case "previous-investigation" -> AssistantIntent.PREVIOUS_INVESTIGATION;
      case "transaction-review" -> AssistantIntent.TRANSACTION_REVIEW;
      case "bad-connections" -> AssistantIntent.BAD_CONNECTIONS;
      case "kyc-profile" -> AssistantIntent.KYC_PROFILE;
      default -> AssistantIntent.KYC_PROFILE;
    };
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
    return mockDataService.buildKycProfileResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getPreviousInvestigationRows(String caseId) {
    return mockDataService.buildPreviousInvestigationResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getTransactionReviewRows(String caseId) {
    return mockDataService.buildTransactionReviewResponse(caseId, assistantQuery()).data();
  }

  private List<Map<String, Object>> getBadConnectionsRows(String caseId) {
    return mockDataService.buildBadConnectionsResponse(caseId, assistantQuery()).data();
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

  private String normalizePrompt(String prompt) {
    return prompt == null
      ? ""
      : prompt.toLowerCase(Locale.ROOT).replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fff]+", " ").trim();
  }

  private boolean containsAny(String value, String... tokens) {
    for (String token : tokens) {
      if (value.contains(token.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }

    return false;
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

  private enum AssistantIntent {
    KYC_PROFILE(
      "KYC Profile Request Recognized",
      "KYC Profile",
      "Please show me the detailed customer information for the current case.",
      "KYC profile mock data"
    ),
    PREVIOUS_INVESTIGATION(
      "Previous Case Request Recognized",
      "Previous Investigation",
      "Please show me the historical case information for the customer in the current case.",
      "Previous investigation mock data"
    ),
    TRANSACTION_REVIEW(
      "Transaction History Request Recognized",
      "Transaction Review",
      "Please show me the historical transfer records for the customer in the current case.",
      "Transaction review mock data"
    ),
    BAD_CONNECTIONS(
      "Bad Connections Request Recognized",
      "Bad Connections",
      "Please show me the bad connections information for the current case.",
      "Bad connections mock data"
    ),
    RISK_ASSESSMENT(
      "Risk Assessment Request Recognized",
      "Risk Assessment",
      "Please assess the risk level of the current case based on all related information.",
      "Aggregated risk assessment"
    ),
    CURRENT_TAB_PREVIEW(
      "Current Tab Request Recognized",
      "Current Tab Preview",
      "Please show me the records for the current workspace tab.",
      "Current tab preview data"
    );

    private final String responseTitle;
    private final String moduleLabel;
    private final String standardEnglishRequest;
    private final String responseTableTitle;

    AssistantIntent(String responseTitle, String moduleLabel, String standardEnglishRequest, String responseTableTitle) {
      this.responseTitle = responseTitle;
      this.moduleLabel = moduleLabel;
      this.standardEnglishRequest = standardEnglishRequest;
      this.responseTableTitle = responseTableTitle;
    }

    public String responseTitle() {
      return responseTitle;
    }

    public String moduleLabel() {
      return moduleLabel;
    }

    public String standardEnglishRequest() {
      return standardEnglishRequest;
    }

    public String responseTableTitle() {
      return responseTableTitle;
    }
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
