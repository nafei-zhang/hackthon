import type { TabKey } from '@/constants/tabs';
import type {
  BadConnectionRow,
  KycProfileRow,
  PreviousInvestigationRow,
  TransactionReviewRow,
} from '@/types/case';
import { formatCurrency, formatDate } from '@/utils/case';
import { Tag } from 'antd';
import type { ProColumns } from '@ant-design/pro-components';

const searchKeywordColumn: ProColumns = {
  title: 'Keyword',
  dataIndex: 'keyword',
  valueType: 'text',
  hideInTable: true,
  fieldProps: {
    placeholder: 'Search within this table',
  },
};

function riskTag(value: string) {
  const colorMap: Record<string, string> = {
    Low: 'green',
    Medium: 'gold',
    High: 'red',
    Pending: 'gold',
    Escalated: 'red',
    Cleared: 'blue',
    'RM managed': 'blue',
    'Not RM managed': 'default',
  };

  return (
    <Tag color={colorMap[value] ?? 'default'} className="table-status-tag">
      {value}
    </Tag>
  );
}

function ellipsisText(value: string, className = 'table-ellipsis-text') {
  return (
    <span className={className} title={value}>
      {value}
    </span>
  );
}

const kycColumns: ProColumns<KycProfileRow>[] = [
  searchKeywordColumn,
  { title: 'Customer ID', dataIndex: 'customerId', copyable: true, minWidth: 120 },
  { title: 'PRC ID', dataIndex: 'prcId', hideInSearch: true, minWidth: 110 },
  { title: 'Entry Permit ID', dataIndex: 'entryPermitId', hideInSearch: true, minWidth: 128 },
  { title: 'CIN Number', dataIndex: 'cinNumber', hideInSearch: true, minWidth: 118 },
  { title: 'Customer Since', dataIndex: 'customerSince', valueType: 'date', hideInSearch: true, minWidth: 120 },
  {
    title: 'RM Managed',
    dataIndex: 'rmManaged',
    minWidth: 148,
    filters: true,
    onFilter: true,
    valueEnum: {
      'RM managed': { text: 'RM managed' },
      'Not RM managed': { text: 'Not RM managed' },
    },
    render: (_, row) => riskTag(row.rmManaged),
  },
  {
    title: 'Address',
    dataIndex: 'address',
    hideInSearch: true,
    minWidth: 180,
    render: (_, row) => ellipsisText(row.address),
  },
  {
    title: 'Email',
    dataIndex: 'email',
    minWidth: 180,
    render: (_, row) => ellipsisText(row.email),
  },
  { title: 'Mobile', dataIndex: 'mobile', hideInSearch: true, minWidth: 126 },
  { title: 'Occupation', dataIndex: 'occupation', minWidth: 118 },
  { title: 'Employer', dataIndex: 'employer', minWidth: 132, render: (_, row) => ellipsisText(row.employer) },
  {
    title: 'Salary',
    dataIndex: 'salary',
    valueType: 'money',
    hideInSearch: true,
    sorter: true,
    minWidth: 116,
    render: (_, row) => formatCurrency(row.salary),
  },
  {
    title: 'Nationality',
    dataIndex: 'nationality',
    minWidth: 116,
    filters: true,
    onFilter: true,
  },
  {
    title: 'My Workplace',
    dataIndex: 'workplace',
    hideInSearch: true,
    minWidth: 128,
    render: (_, row) => ellipsisText(row.workplace),
  },
  { title: 'GSNA', dataIndex: 'gsnaExposure', hideInSearch: true, minWidth: 112 },
];

const previousInvestigationColumns: ProColumns<PreviousInvestigationRow>[] = [
  searchKeywordColumn,
  {
    title: 'Investigation Type',
    dataIndex: 'investigationType',
    filters: true,
    onFilter: true,
  },
  { title: 'Reference Code', dataIndex: 'referenceCode', copyable: true },
  { title: 'Previous Owner', dataIndex: 'previousOwner' },
  {
    title: 'Risk Category',
    dataIndex: 'riskCategory',
    minWidth: 128,
    filters: true,
    onFilter: true,
    render: (_, row) => riskTag(row.riskCategory),
  },
  { title: 'Conclusion', dataIndex: 'conclusion', minWidth: 220, render: (_, row) => ellipsisText(row.conclusion) },
  {
    title: 'Opened At',
    dataIndex: 'openedAt',
    valueType: 'dateTime',
    hideInSearch: true,
    sorter: true,
    render: (_, row) => formatDate(row.openedAt),
  },
  {
    title: 'Closed At',
    dataIndex: 'closedAt',
    valueType: 'dateTime',
    hideInSearch: true,
    sorter: true,
    render: (_, row) => formatDate(row.closedAt),
  },
  { title: 'Note', dataIndex: 'note', hideInSearch: true, minWidth: 240, render: (_, row) => ellipsisText(row.note) },
];

const transactionReviewColumns: ProColumns<TransactionReviewRow>[] = [
  searchKeywordColumn,
  { title: 'Counterparty', dataIndex: 'counterparty' },
  {
    title: 'Instrument Type',
    dataIndex: 'instrumentType',
    minWidth: 132,
    filters: true,
    onFilter: true,
  },
  { title: 'Instrument Name', dataIndex: 'instrumentName' },
  {
    title: 'Amount',
    dataIndex: 'amount',
    valueType: 'money',
    sorter: true,
    hideInSearch: true,
    render: (_, row) => formatCurrency(row.amount, row.currency),
  },
  { title: 'Currency', dataIndex: 'currency', filters: true, onFilter: true },
  {
    title: 'Booking Date',
    dataIndex: 'bookingDate',
    valueType: 'dateTime',
    hideInSearch: true,
    sorter: true,
    render: (_, row) => formatDate(row.bookingDate),
  },
  {
    title: 'Review Status',
    dataIndex: 'reviewStatus',
    minWidth: 132,
    filters: true,
    onFilter: true,
    render: (_, row) => riskTag(row.reviewStatus),
  },
  { title: 'Reviewer', dataIndex: 'reviewer', hideInSearch: true, minWidth: 120 },
  { title: 'Comment', dataIndex: 'comment', hideInSearch: true, minWidth: 220, render: (_, row) => ellipsisText(row.comment) },
];

const badConnectionColumns: ProColumns<BadConnectionRow>[] = [
  searchKeywordColumn,
  { title: 'Device ID', dataIndex: 'deviceId', copyable: true },
  { title: 'IP Address', dataIndex: 'ipAddress', copyable: true },
  {
    title: 'Last Login',
    dataIndex: 'lastLoginAt',
    valueType: 'dateTime',
    hideInSearch: true,
    sorter: true,
    render: (_, row) => formatDate(row.lastLoginAt),
  },
  { title: 'Location', dataIndex: 'location', filters: true, onFilter: true },
  {
    title: 'Risk Level',
    dataIndex: 'riskLevel',
    minWidth: 120,
    filters: true,
    onFilter: true,
    render: (_, row) => riskTag(row.riskLevel),
  },
  { title: 'Relation Type', dataIndex: 'relationType', minWidth: 148 },
  { title: 'Comment', dataIndex: 'comment', hideInSearch: true, minWidth: 220, render: (_, row) => ellipsisText(row.comment) },
];

export function getTableColumns(tabKey: TabKey) {
  const config: Record<TabKey, ProColumns[]> = {
    'kyc-profile': kycColumns,
    'previous-investigation': previousInvestigationColumns,
    'transaction-review': transactionReviewColumns,
    'bad-connections': badConnectionColumns,
  };

  return config[tabKey];
}
