export const CASE_ID_PATTERN = /^CASE-\d{4}-\d{4}$/;

export function isValidCaseId(value: string) {
  return CASE_ID_PATTERN.test(value.trim());
}

export function normalizeCaseId(value: string) {
  return value.trim().toUpperCase();
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatCurrency(value: number, currency = 'USD') {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}
