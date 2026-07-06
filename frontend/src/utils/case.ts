export const CASE_ID_PATTERN = /^[A-Z0-9]{6,32}$/;

export function isValidCaseId(value: string) {
  const normalized = normalizeCaseId(value);
  if (!CASE_ID_PATTERN.test(normalized)) {
    return false;
  }

  const hasLetter = /[A-Z]/.test(normalized);
  const hasDigit = /\d/.test(normalized);
  return hasLetter && hasDigit;
}

export function normalizeCaseId(value: string) {
  return value.replace(/[^a-zA-Z0-9]/g, '').trim().toUpperCase();
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
