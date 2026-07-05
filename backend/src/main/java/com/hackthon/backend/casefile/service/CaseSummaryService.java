package com.hackthon.backend.casefile.service;

import com.hackthon.backend.casefile.model.CaseSummary;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CaseSummaryService {

  private static final List<String> OWNERS = List.of("Lena Wu", "Daniel Ho", "Mira Tan", "Arjun Patel");

  public CaseSummary buildCaseSummary(String caseId) {
    int seed = seedFromCaseId(caseId);
    String updatedAt = utcDateTimeFromJs(2026, 6, (seed % 18) + 1, 10, 30);

    return new CaseSummary(caseId, "ready", pick(OWNERS, seed, 1), updatedAt);
  }

  private int seedFromCaseId(String caseId) {
    return caseId.chars().sum();
  }

  private <T> T pick(List<T> items, int seed, int offset) {
    return items.get(Math.floorMod(seed + offset, items.size()));
  }

  private String utcDateTimeFromJs(int year, int zeroBasedMonth, int day, int hour, int minute) {
    return ZonedDateTime.of(year, 1, 1, hour, minute, 0, 0, ZoneOffset.UTC)
      .plusMonths(zeroBasedMonth)
      .plusDays(day - 1L)
      .toInstant()
      .toString();
  }
}
