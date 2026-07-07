import type { TabKey } from '@/constants/tabs';
import { requestJson } from '@/services/http';
import type {
  CaseSummary,
  PagedResult,
  TableQuery,
  TabDataMap,
  RiskChainData,
} from '@/types/case';

function buildQueryString(query: TableQuery) {
  const searchParams = new URLSearchParams();

  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === '' || value === null) {
      return;
    }

    if (key === 'filters') {
      searchParams.set(key, JSON.stringify(value));
      return;
    }

    searchParams.set(key, String(value));
  });

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : '';
}

export function fetchCaseSummary(caseId: string) {
  return requestJson<CaseSummary>(`/api/cases/${caseId}/summary`);
}

export function fetchTabTableData<T extends TabKey>(
  tabKey: T,
  caseId: string,
  query: TableQuery,
) {
  const queryString = buildQueryString(query);
  return requestJson<PagedResult<TabDataMap[T]>>(`/api/cases/${caseId}/${tabKey}${queryString}`);
}

export function fetchRiskChain(caseId: string) {
  return requestJson<RiskChainData>(`/api/cases/${caseId}/risk-chain`);
}
