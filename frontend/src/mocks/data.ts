import type {
  BadConnectionRow,
  CaseSummary,
  KycProfileRow,
  PreviousInvestigationRow,
  SortOrder,
  TableQuery,
  TransactionReviewRow,
} from '@/types/case';

const owners = ['Lena Wu', 'Daniel Ho', 'Mira Tan', 'Arjun Patel'];
const occupations = ['Consultant', 'Trader', 'Engineer', 'Director'];
const employers = ['Orion Capital', 'Blue Arc Tech', 'Maritime Group', 'GSNA Advisory'];
const nationalities = ['Singaporean', 'Malaysian', 'British', 'Chinese'];
const locations = ['Hong Kong', 'Singapore', 'London', 'Kuala Lumpur'];
const relationTypes = ['Shared device', 'Shared IP', 'Repeated location', 'Dormant overlap'];

type TableRow = Record<string, string | number>;

function seedFromCaseId(caseId: string) {
  return Array.from(caseId).reduce((total, char) => total + char.charCodeAt(0), 0);
}

function pick<T>(items: T[], seed: number, offset: number) {
  return items[(seed + offset) % items.length];
}

function paginate<T>(items: T[], page = 1, pageSize = 10) {
  const start = (page - 1) * pageSize;
  return items.slice(start, start + pageSize);
}

function filterRows<T extends TableRow>(rows: T[], query: TableQuery) {
  const keyword = query.keyword?.toLowerCase().trim() ?? '';
  const globalSearch = query.globalSearch?.toLowerCase().trim() ?? '';
  const filters = query.filters ?? {};

  let result = rows.filter((row) => {
    const values = Object.values(row).join(' ').toLowerCase();
    const matchesKeyword = keyword ? values.includes(keyword) : true;
    const matchesGlobal = globalSearch ? values.includes(globalSearch) : true;
    const matchesFilters = Object.entries(filters).every(([field, expectedValues]) => {
      if (!expectedValues.length) {
        return true;
      }

      return expectedValues.includes(String(row[field] ?? ''));
    });

    return matchesKeyword && matchesGlobal && matchesFilters;
  });

  if (query.sortField && query.sortOrder) {
    const { sortField, sortOrder } = query;
    result = [...result].sort((left, right) => compareValue(left[sortField], right[sortField], sortOrder));
  }

  return result;
}

function compareValue(left: string | number | undefined, right: string | number | undefined, order: SortOrder) {
  if (left === right) {
    return 0;
  }

  const base = left! > right! ? 1 : -1;
  return order === 'ascend' ? base : base * -1;
}

export function buildCaseSummary(caseId: string): CaseSummary {
  const seed = seedFromCaseId(caseId);
  return {
    caseId,
    status: 'ready',
    owner: pick(owners, seed, 1),
    updatedAt: new Date(Date.UTC(2026, 6, (seed % 18) + 1, 10, 30)).toISOString(),
  };
}

export function buildKycProfiles(caseId: string) {
  const seed = seedFromCaseId(caseId);
  return Array.from({ length: 22 }, (_, index): KycProfileRow => ({
    id: `${caseId}-KYC-${index + 1}`,
    customerId: `CUST-${seed + index + 1000}`,
    prcId: `PRC-${seed + index + 2200}`,
    entryPermitId: `EP-${seed + index + 3100}`,
    cinNumber: `CIN-${seed + index + 4100}`,
    customerSince: `${2014 + (index % 8)}-0${(index % 9) + 1}-15`,
    rmManaged: index % 2 === 0 ? 'RM managed' : 'Not RM managed',
    address: `${20 + index} Queen's Road, ${pick(locations, seed, index)}`,
    email: `customer${index + 1}@case-lab.com`,
    mobile: `+65 98${(seed + index).toString().padStart(6, '0').slice(0, 6)}`,
    occupation: pick(occupations, seed, index),
    employer: pick(employers, seed, index + 2),
    salary: 48000 + index * 6500,
    nationality: pick(nationalities, seed, index),
    workplace: pick(['My Workplace', 'Corporate Desk', 'Regional Hub'], seed, index),
    gsnaExposure: pick(['HSBC MY', 'HSBC UK', 'HSBC US', 'HSBC HK'], seed, index),
  }));
}

export function buildPreviousInvestigations(caseId: string) {
  const seed = seedFromCaseId(caseId);
  return Array.from({ length: 18 }, (_, index): PreviousInvestigationRow => ({
    id: `${caseId}-PI-${index + 1}`,
    investigationType: pick(['CAT A', 'CAT B', 'CAT C', 'EDD'], seed, index),
    referenceCode: `UCM-${seed + 500 + index}`,
    previousOwner: pick(owners, seed, index),
    riskCategory: pick(['Low', 'Medium', 'High'], seed, index),
    conclusion: pick(['Closed with note', 'Escalated to UCM', 'Monitoring only'], seed, index + 1),
    openedAt: new Date(Date.UTC(2025, index % 12, 5 + index)).toISOString(),
    closedAt: new Date(Date.UTC(2025, (index % 12) + 1, 12 + index)).toISOString(),
    note: `Prior case narrative ${index + 1} for ${caseId}`,
  }));
}

export function buildTransactionReviews(caseId: string) {
  const seed = seedFromCaseId(caseId);
  return Array.from({ length: 24 }, (_, index): TransactionReviewRow => ({
    id: `${caseId}-TR-${index + 1}`,
    counterparty: pick(['Atlas Holdings', 'North Ridge', 'Silver Axis', 'Morning Peak'], seed, index),
    instrumentType: pick(['Cheque', 'Stock'], seed, index),
    instrumentName: pick(['AIA', 'HSBC', 'Tencent', 'Petronas'], seed, index + 1),
    amount: 12000 + index * 5700,
    currency: pick(['USD', 'HKD', 'SGD'], seed, index),
    bookingDate: new Date(Date.UTC(2026, index % 6, 2 + index)).toISOString(),
    reviewStatus: pick(['Pending', 'Escalated', 'Cleared'], seed, index),
    reviewer: pick(owners, seed, index + 2),
    comment: `Counterparty and instrument review ${index + 1}`,
  }));
}

export function buildBadConnections(caseId: string) {
  const seed = seedFromCaseId(caseId);
  return Array.from({ length: 16 }, (_, index): BadConnectionRow => ({
    id: `${caseId}-BC-${index + 1}`,
    deviceId: `DEV-${seed + 800 + index}`,
    ipAddress: `10.${(seed + index) % 255}.${(seed + index * 2) % 255}.${50 + index}`,
    lastLoginAt: new Date(Date.UTC(2026, 5, 1 + index, 7 + (index % 8), 20)).toISOString(),
    location: pick(locations, seed, index),
    riskLevel: pick(['Low', 'Medium', 'High'], seed, index),
    relationType: pick(relationTypes, seed, index),
    comment: `Observed overlap pattern ${index + 1}`,
  }));
}

export function buildPagedResponse<T extends TableRow>(rows: T[], query: TableQuery) {
  const filtered = filterRows(rows, query);
  const page = Number(query.page ?? 1);
  const pageSize = Number(query.pageSize ?? 10);

  return {
    data: paginate(filtered, page, pageSize),
    total: filtered.length,
    success: true,
  };
}

export function buildAssistantHtml(prompt: string, caseId: string | undefined, activeTab: string) {
  return `
    <section>
      <h3>Case guidance summary</h3>
      <p><strong>Question:</strong> ${prompt}</p>
      <p><strong>Case context:</strong> ${caseId || 'No case ID has been set yet.'}</p>
      <p><strong>Active tab:</strong> ${activeTab}</p>
      <ul>
        <li>Review the key entities shown in the current tab before escalating.</li>
        <li>Use the global search and column filters to narrow the working set.</li>
        <li>Confirm whether the current case requires follow-up from the assigned owner.</li>
      </ul>
      <p>This answer is returned in HTML so the client can render formatted content directly.</p>
    </section>
  `;
}

export function buildAssistantStreamChunks(prompt: string, caseId: string | undefined, activeTab: string) {
  const resolvedCaseId = caseId || 'FC260305617670';
  const tabLabel = activeTab
    .replace(/-/g, ' ')
    .replace(/\b\w/g, (char: string) => char.toUpperCase());
  const previewRows: TableRow[] = (
    activeTab === 'previous-investigation'
      ? buildPreviousInvestigations(resolvedCaseId)
      : activeTab === 'transaction-review'
        ? buildTransactionReviews(resolvedCaseId)
        : activeTab === 'bad-connections'
          ? buildBadConnections(resolvedCaseId)
          : buildKycProfiles(resolvedCaseId)
  ).slice(0, 3);
  const columns = Object.keys(previewRows[0] ?? {}).slice(0, 5);
  const table = !previewRows.length
    ? 'No preview data available.'
    : [
        `| ${columns.map((column) => column.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/\b\w/g, (char: string) => char.toUpperCase())).join(' | ')} |`,
        `| ${columns.map(() => '---').join(' | ')} |`,
        ...previewRows.map((row: TableRow) => `| ${columns.map((column) => String(row[column] ?? '').replace(/\|/g, '\\|')).join(' | ')} |`),
      ].join('\n');
  
  // Add mermaid diagram if user asks for diagram/flowchart/chart
  const includeMermaid = prompt.toLowerCase().includes('diagram') || 
                        prompt.toLowerCase().includes('flowchart') || 
                        prompt.toLowerCase().includes('chart') ||
                        prompt.toLowerCase().includes('mermaid');
  
  const mermaidDiagram = includeMermaid ? `
### Case Relationship Diagram

This diagram shows the relationship flow in the investigation:

\`\`\`mermaid
graph TD
    A[Case Initiation] --> B[KYC Verification]
    B --> C{Risk Assessment}
    C -->|Low Risk| D[Standard Review]
    C -->|Medium Risk| E[Enhanced Review]
    C -->|High/Critical| F[Escalation]
    D --> G[Case Closure]
    E --> G
    F --> H[Investigation Team]
    H --> G
\`\`\`

` : '';
  
  const text =
    `## Case guidance summary\n\n` +
    `**Question:** ${prompt}  \n` +
    `**Case:** ${caseId || 'Not set (previewing FC260305617670)'}  \n` +
    `**Active tab:** ${tabLabel}  \n` +
    `**Preview rows:** ${previewRows.length}\n\n` +
    `### Recommended review actions\n\n` +
    `- Validate the current table results before escalating.\n` +
    `- Cross-check key identifiers shown in the active tab.\n` +
    `- Capture any suspicious pattern for follow-up review.\n\n` +
    mermaidDiagram +
    `### Mock table preview\n\n` +
    `${table}\n\n` +
    `_Streaming response rendered as markdown in the client._`;

  return text.split(/(\s+)/).filter(Boolean);
}

export function buildRiskChainData(caseId: string) {
  const seed = seedFromCaseId(caseId);
  const riskLevels = ['Low', 'Medium', 'High', 'Critical'] as const;
  const customerNames = ['Lena Wu', 'Daniel Ho', 'Mira Tan', 'Arjun Patel', 'Sarah Chen', 'Mike Liu', 'Emma Wang', 'Tom Zhang'];
  const companyNames = ['Orion Capital', 'Blue Arc Tech', 'Maritime Group', 'GSNA Advisory', 'Nexus Holdings', 'Apex Corp'];
  const relationLabels = ['Direct relation', 'Shared ownership', 'Business partner', 'Financial transaction', 'Family relation'];
  const industries = ['Finance', 'Technology', 'Logistics', 'Consulting', 'Manufacturing'];
  const emailDomains = ['case-lab.com', 'finance.net', 'techmail.org', 'bizmail.hk'];

  const nodes: any[] = [];
  const edges: any[] = [];

  nodes.push({
    id: 'main-customer',
    type: 'customer',
    name: pick(customerNames, seed, 0),
    customerId: `CUST-${seed + 1000}`,
    riskLevel: pick(riskLevels, seed, 1),
    isPrimary: true,
    details: {
      email: `customer.${seed + 1000}@${pick(emailDomains, seed, 0)}`,
      mobile: `+65 98${(seed + 1000).toString().padStart(6, '0').slice(0, 6)}`,
      occupation: pick(occupations, seed, 0),
      employer: pick(employers, seed, 0),
      salary: 48000 + (seed % 10) * 6500,
      nationality: pick(nationalities, seed, 0),
      address: `${seed + 20} Queen's Road, ${pick(locations, seed, 0)}`,
      customerSince: `${2014 + (seed % 8)}-${((seed % 9) + 1).toString().padStart(2, '0')}-15`,
    },
  });

  const nodeCount = 5 + (seed % 3);
  for (let i = 0; i < nodeCount; i++) {
    const nodeType = i % 2 === 0 ? 'customer' : 'company';
    const name = nodeType === 'customer' ? pick(customerNames, seed, i + 1) : pick(companyNames, seed, i);

    const node: any = {
      id: `node-${i}`,
      type: nodeType,
      name: name,
      customerId: nodeType === 'customer' ? `CUST-${seed + 1001 + i}` : undefined,
      riskLevel: pick(riskLevels, seed, i + 2),
      isPrimary: false,
    };

    if (nodeType === 'customer') {
      node.details = {
        email: `customer.${seed + 1001 + i}@${pick(emailDomains, seed, i + 1)}`,
        mobile: `+65 98${(seed + 1001 + i).toString().padStart(6, '0').slice(0, 6)}`,
        occupation: pick(occupations, seed, i + 1),
        employer: pick(employers, seed, i + 2),
        salary: 48000 + (i + 1) * 6500,
        nationality: pick(nationalities, seed, i + 1),
        address: `${seed + 20 + i + 1} Queen's Road, ${pick(locations, seed, i + 1)}`,
        customerSince: `${2014 + ((i + 1) % 8)}-${(((i + 1) % 9) + 1).toString().padStart(2, '0')}-15`,
      };
    } else {
      node.details = {
        registrationNumber: `REG-${seed + 2000 + i}`,
        industry: pick(industries, seed, i),
        foundedYear: 1990 + (seed % 30) + i,
        headquarters: pick(locations, seed, i),
        employees: 50 + (seed % 200) + i * 20,
        revenue: 1000000 + (seed % 5000000) + i * 500000,
      };
    }

    nodes.push(node);
  }

  for (let i = 0; i < nodeCount; i++) {
    edges.push({
      id: `edge-main-${i}`,
      source: 'main-customer',
      target: `node-${i}`,
      label: pick(relationLabels, seed, i),
    });
  }

  for (let i = 0; i < nodeCount - 1; i += 2) {
    if (i + 1 < nodeCount) {
      edges.push({
        id: `edge-${i}-${i + 1}`,
        source: `node-${i}`,
        target: `node-${i + 1}`,
        label: pick(relationLabels, seed, i + nodeCount),
      });
    }
  }

  return { nodes, edges };
}
