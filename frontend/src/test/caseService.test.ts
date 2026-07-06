import { fetchTabTableData } from '@/services/caseService';
import { describe, expect, it } from 'vitest';

describe('caseService integration', () => {
  it('returns filtered results for column-level filters', async () => {
    const response = await fetchTabTableData('kyc-profile', 'FC260305617670', {
      page: 1,
      pageSize: 20,
      filters: {
        rmManaged: ['RM managed'],
      },
    });

    expect(response.success).toBe(true);
    expect(response.data.length).toBeGreaterThan(0);
    expect(response.data.every((item) => item.rmManaged === 'RM managed')).toBe(true);
  });
});
