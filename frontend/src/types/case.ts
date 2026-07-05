import type { TabKey } from '@/constants/tabs';

export type CaseStatus = 'ready' | 'pending_case_id';
export type SortOrder = 'ascend' | 'descend';

export type CaseSummary = {
  caseId: string;
  status: CaseStatus;
  owner: string;
  updatedAt: string;
};

export type TableQuery = {
  page?: number;
  pageSize?: number;
  keyword?: string;
  globalSearch?: string;
  sortField?: string;
  sortOrder?: SortOrder;
  filters?: Record<string, string[]>;
};

export type PagedResult<T> = {
  data: T[];
  total: number;
  success: boolean;
};

export type KycProfileRow = {
  id: string;
  customerId: string;
  prcId: string;
  entryPermitId: string;
  cinNumber: string;
  customerSince: string;
  rmManaged: 'RM managed' | 'Not RM managed';
  address: string;
  email: string;
  mobile: string;
  occupation: string;
  employer: string;
  salary: number;
  nationality: string;
  workplace: string;
  gsnaExposure: string;
};

export type PreviousInvestigationRow = {
  id: string;
  investigationType: string;
  referenceCode: string;
  previousOwner: string;
  riskCategory: 'Low' | 'Medium' | 'High';
  conclusion: string;
  openedAt: string;
  closedAt: string;
  note: string;
};

export type TransactionReviewRow = {
  id: string;
  counterparty: string;
  instrumentType: 'Cheque' | 'Stock';
  instrumentName: string;
  amount: number;
  currency: string;
  bookingDate: string;
  reviewStatus: 'Pending' | 'Escalated' | 'Cleared';
  reviewer: string;
  comment: string;
};

export type BadConnectionRow = {
  id: string;
  deviceId: string;
  ipAddress: string;
  lastLoginAt: string;
  location: string;
  riskLevel: 'Low' | 'Medium' | 'High';
  relationType: string;
  comment: string;
};

export type TabDataMap = {
  'kyc-profile': KycProfileRow;
  'previous-investigation': PreviousInvestigationRow;
  'transaction-review': TransactionReviewRow;
  'bad-connections': BadConnectionRow;
};

export type TabRequestConfig = {
  tabKey: TabKey;
  caseId: string;
  query: TableQuery;
};
