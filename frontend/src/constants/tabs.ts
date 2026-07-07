export const TAB_ITEMS = [
  { key: 'risk-chain', label: 'Risk Chain' },
  { key: 'kyc-profile', label: 'KYC profile' },
  { key: 'previous-investigation', label: 'Previous Investigation' },
  { key: 'transaction-review', label: 'Transaction review' },
  { key: 'bad-connections', label: 'Bad connections' },
] as const;

export type TabKey = (typeof TAB_ITEMS)[number]['key'];

export const DEFAULT_TAB_KEY: TabKey = 'risk-chain';

export const TAB_LABEL_MAP: Record<TabKey, string> = TAB_ITEMS.reduce(
  (accumulator, item) => ({
    ...accumulator,
    [item.key]: item.label,
  }),
  {} as Record<TabKey, string>,
);
